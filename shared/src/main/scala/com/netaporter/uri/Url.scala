package com.netaporter.uri

import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.parsing.UrlParser

import scala.collection.{GenTraversable, GenTraversableOnce, Seq}

/**
  * Represents a URL, which will be one of these forms:
  *
  *  -           Absolute: `http://example.com`
  *  -  Protocol Relative: `//example.com`
  *  -  Without Authority: `mailto:example@example.com`
  *  -      Root Relative: `/index.html?a=b`
  *  -  Rootless Relative: `index.html?a=b`
  *  -  Rootless Relative
  *    (with doc segment): `../index.html?a=b`
  */
trait Url extends Uri {
  type Self <: Url
  type SelfWithScheme <: Url
  type SelfWithAuthority <: UrlWithAuthority

  def hostOption: Option[Host]
  def port: Option[Int]

  def user: Option[String]
  def password: Option[String]

  def path: UrlPath
  def query: QueryString
  def fragment: Option[String]

  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String]

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Vector[String]

  /**
    * Returns the second largest subdomain for this URL's host.
    *
    * E.g. for http://a.b.c.example.com returns a.b.c
    *
    * Note: In the event there is only one subdomain (i.e. the host is the root domain), this method returns `None`.
    * E.g. This method will return `None` for `http://example.com`.
    *
    * @return the second largest subdomain for this URL's host
    */
  def subdomain: Option[String]

  /**
    * Returns all subdomains for this URL's host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    * @return all subdomains for this URL's host
    */
  def subdomains: Vector[String]

  /**
    * Returns the shortest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a
    * @return the shortest subdomain for this URL's host
    */
  def shortestSubdomain: Option[String]

  /**
    * Returns the longest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    * @return the longest subdomain for this URL's host
    */
  def longestSubdomain: Option[String]

  /**
    * Copies this Url but with the authority set as the given value.
    *
    * @param authority the authority host to set
    * @return a new Url with the specified authority
    */
  def withAuthority(authority: Authority): SelfWithAuthority

  /**
    * Copies this Url but with the host set as the given value.
    *
    * @param host the new host to set
    * @return a new Url with the specified host
    */
  def withHost(host: Host): SelfWithAuthority

  /**
    * Copies this Url but with the host set as the given value.
    *
    * @param host the new host to set
    * @return a new Url with the specified host
    */
  def withHost(host: String): SelfWithAuthority =
    withHost(Host.parse(host))

  /**
    * Copies this Url but with the user set as the given value.
    *
    * @param user the new user to set
    * @return a new Url with the specified user
    */
  def withUser(user: String): SelfWithAuthority

  /**
    * Copies this Url but with the password set as the given value.
    *
    * @param password the new password to set
    * @return a new Url with the specified password
    */
  def withPassword(password: String): SelfWithAuthority

  /**
    * Copies this Url but with the port set as the given value.
    *
    * @param port the new port to set
    * @return a new Url with the specified port
    */
  def withPort(port: Int): SelfWithAuthority

  /**
    * Copies this Url but with the fragment set as the given value.
    *
    * @param fragment the new fragment to set
    * @return a new Url with the specified fragment
    */
  def withFragment(fragment: String): Self =
    withFragment(Some(fragment))

  /**
    * Copies this Url but with the fragment set as the given value.
    *
    * @param fragment the new fragment to set
    * @return a new Url with the specified fragment
    */
  def withFragment(fragment: Option[String]): Self

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): Self

  /**
    * Copies this Url but with the query set as the given value.
    *
    * @param query the new QueryString to set
    * @return a new Url with the specified query
    */
  def withQueryString(query: QueryString): Self

  /**
    * Replaces the all existing Query String parameters with a new set of query params
    *
    * Pairs with values, such as ("k", Some("v")), represent query params with values, i.e ?k=v
    * Pairs without values, such as ("k", None), represent query params without values, i.e ?k
    */
  def withQueryStringOptionValues(params: (String, Option[String])*): Self =
    withQueryString(QueryString(params.toVector))

  /**
    * Replaces the all existing Query String parameters with a new set of query params
    */
  def withQueryString(params: (String, String)*): Self =
    withQueryString(QueryString.fromTraversable(params))

  def addPathPart(part: String): Self =
    withPath(path.addPart(part))

  def addPathParts(parts: GenTraversableOnce[String]): Self =
    withPath(path.addParts(parts))

  def addPathParts(parts: String*): Self =
    withPath(path.addParts(parts))
  /**
    * Adds a new Query String parameter key-value pair.
    *
    * Pairs with values, such as `Some("value")`, represent query params with values, i.e `?param=value`
    * Pairs without values, i.e `None`, represent query params without values, i.e `?param`
    *
    * @param name name of the parameter
    * @param value value for the parameter
    * @return A new Url with the new Query String parameter
    */
  def addParam(name: String, value: Option[String]): Self =
    withQueryString(query.addParam(name, value))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * @param name name of the parameter
    * @param value value for the parameter
    * @return A new Url with the new Query String parameter
    */
  def addParam(name: String, value: String): Self =
    withQueryString(query.addParam(name, value))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * @param kv name-value pair for the query parameter to be added
    * @return A new Url with the new Query String parameter
    */
  def addParam(kv: (String, String)): Self =
    withQueryString(query.addParam(kv))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    * Pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    *
    * @param kv name-value pair for the query parameter to be added
    * @return A new Url with the new Query String parameter
    */
  def addParamOptionValue(kv: (String, Option[String])): Self =
    withQueryString(query.addParamOptionValue(kv))

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * @param kvs A list of key-value pairs to add as query parameters
    * @return A new Url with the new Query String parameters
    */
  def addParams(kvs: (String, String)*): Self =
    withQueryString(query.addParams(kvs: _*))

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * @param kvs A list of key-value pairs to add as query parameters
    * @return A new Url with the new Query String parameters
    */
  def addParams(kvs: GenTraversable[(String, String)]): Self =
    withQueryString(query.addParams(kvs))

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    * Pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    *
    * @param kvs A list of key-value pairs to add as query parameters
    * @return A new Url with the new Query String parameters
    */
  def addParamsOptionValues(kvs: GenTraversable[(String, Option[String])]): Self =
    withQueryString(query.addParamsOptionValues(kvs))

  /**
    * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
    * with the specified value.
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    * Pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    *
    * @param k Key for the Query String parameter(s) to replace
    * @param v value to replace with
    * @return A new Uri with the result of the replace
    */
  def replaceParams(k: String, v: Option[String]): Self =
    withQueryString(query.replaceAll(k, v))

  /**
    * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
    * with the specified value.
    *
    * @param k Key for the Query String parameter(s) to replace
    * @param v value to replace with
    * @return A new Uri with the result of the replace
    */
  def replaceParams(k: String, v: String): Self =
    withQueryString(query.replaceAll(k, v))

  /**
    * Removes all Query String parameters with the specified key
    * @param k Key for the Query String parameter(s) to remove
    * @return
    */
  def removeParams(k: String): Self = {
    withQueryString(query.removeAll(k))
  }

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeParams(k: String*): Self = {
    withQueryString(query.removeAll(k))
  }

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeParams(k: GenTraversableOnce[String]): Self = {
    withQueryString(query.removeAll(k))
  }

  /**
    * Removes all Query String parameters
    * @return
    */
  def removeQueryString() = {
    withQueryString(QueryString.empty)
  }

  /**
    * Transforms the Query String by applying the specified Function to each Query String Parameter
    *
    * @param f A function that returns a new Parameter when applied to each Parameter
    * @return
    */
  def mapQuery(f: ((String, Option[String])) => (String, Option[String])): Self =
    withQueryString(query.map(f))

  /**
    * Transforms the Query String by applying the specified Function to each Query String Parameter
    *
    * @param f A function that returns a collection of Parameters when applied to each parameter
    * @return
    */
  def flatMapQuery(f: ((String, Option[String])) => GenTraversableOnce[(String, Option[String])]): Self =
    withQueryString(query.flatMap(f))

  /**
    * Transforms the Query String by applying the specified Function to each Query String Parameter name
    *
    * @param f A function that returns a new Parameter name when applied to each Parameter name
    * @return
    */
  def mapQueryNames(f: String => String): Self =
    withQueryString(query.mapNames(f))

  /**
    * Transforms the Query String by applying the specified Function to each Query String Parameter value
    *
    * @param f A function that returns a new Parameter value when applied to each Parameter value
    * @return
    */
  def mapQueryValues(f: String => String): Self =
    withQueryString(query.mapValues(f))

  /**
    * Removes any Query String Parameters that return false when applied to the given Function
    *
    * @param f
    * @return
    */
  def filterQuery(f: ((String, Option[String])) => Boolean): Self =
    withQueryString(query.filter(f))

  /**
    * Removes any Query String Parameters that return false when their name is applied to the given Function
    *
    * @param f
    * @return
    */
  def filterQueryNames(f: String => Boolean): Self =
    withQueryString(query.filterNames(f))

  /**
    * Removes any Query String Parameters that return false when their value is applied to the given Function
    *
    * @param f
    * @return
    */
  def filterQueryValues(f: String => Boolean): Self =
    withQueryString(query.filterValues(f))

  private[uri] def fragmentToString(c: UriConfig): String =
    fragment.map(f => "#" + c.fragmentEncoder.encode(f, c.charset)).getOrElse("")

  def toUrl: Url = this
  def toUrn: Urn = throw new IllegalStateException(getClass.getSimpleName + " cannot be cast to Urn")
}

object Url {

  def apply(scheme: String = null,
            user: String = null,
            password: String = null,
            host: String = null,
            port: Int = -1,
            path: String = "",
            query: QueryString = QueryString.empty,
            fragment: String = null)
           (implicit config: UriConfig = UriConfig.default): Url = {

    val urlPath = UrlPath.parse(path)
    val frag = Option(fragment)
    def authority = {
      require(urlPath.isEmpty || urlPath.leadingSlash,
              "A Url with an authority must either have an empty path or a path that begins with " +
              "a slash ('/') character. RFC 3986 section 3.2.")
      val portOpt = if(port > 0) Some(port) else None
      Authority(UserInfo(Option(user), Option(password)), Host.parse(host), portOpt)
    }

    (scheme, host) match {
      case (null, null) => RelativeUrl(urlPath, query, frag)
      case (   _, null) => UrlWithoutAuthority(scheme, urlPath, query, frag)
      case (null,    _) => ProtocolRelativeUrl(authority, urlPath, query, frag)
      case (   _,    _) => AbsoluteUrl(scheme, authority, urlPath, query, frag)
    }
  }

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Url =
    UrlParser.parseUrl(s.toString)
}

/**
  * Represents Relative URLs which do not contain an authority. Examples include:
  *
  *  -      Root Relative: `/index.html?a=b`
  *  -  Rootless Relative: `index.html?a=b`
  *  -  Rootless Relative
  *    (with dot segment): `../index.html?a=b`
  */
case class RelativeUrl(path: UrlPath,
                       query: QueryString,
                       fragment: Option[String])
                      (implicit val config: UriConfig = UriConfig.default) extends Url {

  type Self = RelativeUrl
  type SelfWithAuthority = ProtocolRelativeUrl
  type SelfWithScheme = UrlWithoutAuthority

  def self: RelativeUrl = this

  def schemeOption: Option[String] = None

  def hostOption: Option[Host] = None
  def port: Option[Int] = None

  def user: Option[String] = None
  def password: Option[String] = None

  def publicSuffix: Option[String] = None
  def publicSuffixes: Vector[String] = Vector.empty
  def subdomain: Option[String] = None
  def subdomains: Vector[String] = Vector.empty
  def shortestSubdomain: Option[String] = None
  def longestSubdomain: Option[String] = None

  def withScheme(scheme: String): UrlWithoutAuthority =
    UrlWithoutAuthority(scheme, path, query, fragment)

  def withAuthority(authority: Authority): ProtocolRelativeUrl =
    ProtocolRelativeUrl(authority, path, query, fragment)

  def withHost(host: Host): ProtocolRelativeUrl =
    withAuthority(Authority(host))

  def withUser(user: String): ProtocolRelativeUrl =
    withAuthority(Authority(UserInfo(user), host = Host.empty, port = None))

  def withPassword(password: String): ProtocolRelativeUrl =
    withAuthority(Authority(UserInfo(user = None, Some(password)), host = Host.empty, port = None))

  def withPort(port: Int): ProtocolRelativeUrl =
    withAuthority(Authority(host = "", port))

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): RelativeUrl =
    copy(path = path)


  def withFragment(fragment: Option[String]): RelativeUrl =
    copy(fragment = fragment)


  def withQueryString(query: QueryString): RelativeUrl =
    copy(query = query)

  private[uri] def toString(c: UriConfig): String =
    path.toString(c) + query.toString(c) + fragmentToString(c)
}

object RelativeUrl {
  def empty: RelativeUrl =
    RelativeUrl(UrlPath.empty, QueryString.empty, None)

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): RelativeUrl =
    UrlParser.parseRelativeUrl(s.toString)
}

/**
  * Represents absolute URLs with an authority (i.e. URLs with a host), examples include:
  *
  *  -          Absolute URL: `http://example.com`
  *  - Protocol Relative URL: `//example.com`
  */
trait UrlWithAuthority extends Url {

  type Self <: UrlWithAuthority
  type SelfWithScheme <: UrlWithAuthority
  type SelfWithAuthority = Self

  def authority: Authority

  def host: Host = authority.host
  def hostOption: Option[Host] = Some(host)

  def port: Option[Int] = authority.port
  def userInfo: UserInfo = authority.userInfo
  def user: Option[String] = authority.user
  def password: Option[String] = authority.password

  def withHost(host: Host): Self =
    withAuthority(authority.copy(host = host))

  def withUser(user: String): Self = {
    val newUserInfo = userInfo.copy(user = Some(user))
    withAuthority(authority.copy(userInfo = newUserInfo))
  }

  def withPassword(password: String): Self = {
    val newUserInfo = userInfo.copy(password = Some(password))
    withAuthority(authority.copy(userInfo = newUserInfo))
  }

  def withPort(port: Int): Self =
    withAuthority(authority.copy(port = Some(port)))

  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String] =
    authority.publicSuffix

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Vector[String] =
    authority.publicSuffixes

  /**
    * Returns the second largest subdomain for this URL's host.
    *
    * E.g. for http://a.b.c.example.com returns a.b.c
    *
    * Note: In the event there is only one subdomain (i.e. the host is the root domain), this method returns `None`.
    * E.g. This method will return `None` for `http://example.com`.
    *
    * @return the second largest subdomain for this URL's host
    */
  def subdomain: Option[String] =
    authority.subdomain

  /**
    * Returns all subdomains for this URL's host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    * @return all subdomains for this URL's host
    */
  def subdomains: Vector[String] =
    authority.subdomains

  /**
    * Returns the shortest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a
    * @return the shortest subdomain for this URL's host
    */
  def shortestSubdomain: Option[String] =
    authority.shortestSubdomain

  /**
    * Returns the longest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    * @return the longest subdomain for this URL's host
    */
  def longestSubdomain: Option[String] =
    authority.longestSubdomain
}

object UrlWithAuthority {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrlWithAuthority =
    UrlParser.parseUrlWithAuthority(s.toString)
}

/**
  * Represents absolute URLs, for example: `//example.com`
  */
case class ProtocolRelativeUrl(authority: Authority,
                               path: UrlPath,
                               query: QueryString,
                               fragment: Option[String])
                              (implicit val config: UriConfig = UriConfig.default) extends UrlWithAuthority {

  type Self = ProtocolRelativeUrl
  type SelfWithScheme = AbsoluteUrl

  def self: ProtocolRelativeUrl = this

  def schemeOption: Option[String] = None

  def withScheme(scheme: String): AbsoluteUrl =
    AbsoluteUrl(scheme, authority, path, query, fragment)

  def withAuthority(authority: Authority): ProtocolRelativeUrl =
    copy(authority = authority)

  def withFragment(fragment: Option[String]): ProtocolRelativeUrl =
    copy(fragment = fragment)

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): ProtocolRelativeUrl =
    copy(path = path)

  def withQueryString(query: QueryString): ProtocolRelativeUrl =
    copy(query = query)

  private[uri] def toString(c: UriConfig): String =
    "//" + authority.toString(c) + path.toString(c) + query.toString(c) + fragmentToString(c)
}

object ProtocolRelativeUrl {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): ProtocolRelativeUrl =
    UrlParser.parseProtocolRelativeUrl(s.toString)
}

/**
  * Represents absolute URLs, for example: `http://example.com`
  */
case class AbsoluteUrl(scheme: String,
                       authority: Authority,
                       path: UrlPath,
                       query: QueryString,
                       fragment: Option[String])
                      (implicit val config: UriConfig = UriConfig.default) extends UrlWithAuthority {

  type Self = AbsoluteUrl
  type SelfWithScheme = AbsoluteUrl

  def self: AbsoluteUrl = this

  def schemeOption: Option[String] = Some(scheme)

  def withScheme(scheme: String): AbsoluteUrl =
    AbsoluteUrl(scheme, authority, path, query, fragment)

  def withAuthority(authority: Authority): AbsoluteUrl =
    copy(authority = authority)

  def withFragment(fragment: Option[String]): AbsoluteUrl =
    copy(fragment = fragment)

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): AbsoluteUrl =
    copy(path = path)

  def withQueryString(query: QueryString): AbsoluteUrl =
    copy(query = query)

  private[uri] def toString(c: UriConfig): String =
    scheme + "://" + authority.toString(c) + path.toString(c) + query.toString(c) + fragmentToString(c)
}

object AbsoluteUrl {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): AbsoluteUrl =
    UrlParser.parseAbsoluteUrl(s.toString)
}

/**
  * Represents URLs that do not have an authority, for example: `mailto:example@example.com`
  */
case class UrlWithoutAuthority(scheme: String,
                               path: UrlPath,
                               query: QueryString,
                               fragment: Option[String])
                              (implicit val config: UriConfig = UriConfig.default) extends Url {

  type Self = UrlWithoutAuthority
  type SelfWithScheme = UrlWithoutAuthority
  type SelfWithAuthority = AbsoluteUrl

  def self: UrlWithoutAuthority = this

  def schemeOption: Option[String] = Some(scheme)
  def hostOption: Option[Host] = None
  def port: Option[Int] = None
  def user: Option[String] = None
  def password: Option[String] = None

  def publicSuffix: Option[String] = None
  def publicSuffixes: Vector[String] = Vector.empty
  def subdomain: Option[String] = None
  def subdomains: Vector[String] = Vector.empty
  def shortestSubdomain: Option[String] = None
  def longestSubdomain: Option[String] = None

  def withScheme(scheme: String): UrlWithoutAuthority =
    copy(scheme = scheme)

  /**
    * Copies this Url but with the host set as the given value.
    *
    * @param host the new host to set
    * @return a new Url with the specified host
    */
  def withHost(host: Host): AbsoluteUrl =
    AbsoluteUrl(scheme, Authority(host), path, query, fragment)

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): UrlWithoutAuthority =
    copy(path = path)

  /**
    * Copies this Url but with the port set as the given value.
    *
    * @param port the new port to set
    * @return a new Url with the specified port
    */
  def withPort(port: Int): AbsoluteUrl =
    AbsoluteUrl(scheme, Authority(host = "", port), path, query, fragment)

  /**
    * Copies this Url but with the user set as the given value.
    *
    * @param user the new user to set
    * @return a new Url with the specified user
    */
  def withUser(user: String): AbsoluteUrl =
    AbsoluteUrl(scheme, Authority(UserInfo(user), host = Host.empty, port = None), path, query, fragment)

  /**
    * Copies this Url but with the password set as the given value.
    *
    * @param password the new password to set
    * @return a new Url with the specified password
    */
  def withPassword(password: String): AbsoluteUrl =
    AbsoluteUrl(scheme, Authority(UserInfo(user = None, password = Some(password)), host = Host.empty, port = None), path, query, fragment)

  def withAuthority(authority: Authority): AbsoluteUrl =
    AbsoluteUrl(scheme, authority, path, query, fragment)

  def withFragment(fragment: Option[String]): UrlWithoutAuthority =
    copy(fragment = fragment)

  def withQueryString(query: QueryString): UrlWithoutAuthority =
    copy(query = query)

  private[uri] def toString(c: UriConfig): String =
    scheme + ":" + path.toString(c) + query.toString(c) + fragmentToString(c)
}

object UrlWithoutAuthority {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrlWithoutAuthority =
    UrlParser.parseUrlWithoutAuthority(s.toString)
}

case class UrlPath(parts: Vector[String], leadingSlash: Boolean = true)(implicit val config: UriConfig = UriConfig.default) extends Path {

  def withConfig(config: UriConfig): Path = UrlPath(parts, leadingSlash)(config)

  def isEmpty: Boolean =
    parts.isEmpty && !leadingSlash

  def addPart(part: String): UrlPath = {
    if(part.isEmpty && this == UrlPath.empty)
      UrlPath.slash
    else
      copy(parts = parts :+ part)
  }

  def addParts(otherParts: String*): UrlPath = {
    addParts(otherParts)
  }

  def addParts(otherParts: GenTraversableOnce[String]): UrlPath = {
    copy(parts = parts ++ otherParts)
  }

  /**
    * Returns the encoded path. By default non ASCII characters in the path are percent encoded.
    * @return String containing the path for this Uri
    */
  private[uri] def toString(c: UriConfig): String =
    if(parts.isEmpty) ""
    else {
      val encodedParts = parts.map(p => c.pathEncoder.encode(p, c.charset))
      (if(leadingSlash) "/" else "") + encodedParts.mkString("/")
    }
}

object UrlPath {
  val empty = UrlPath(Vector.empty, leadingSlash = false)
  val slash = UrlPath(Vector.empty, leadingSlash = true)

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrlPath =
    UrlParser.parsePath(s.toString)
}