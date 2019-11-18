package io.roflsoft.http.server

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}

import akka.http.scaladsl.HttpsConnectionContext
import akka.http.scaladsl.ConnectionContext
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

class HttpsConfig(
   password: Array[Char],
   keyStore: KeyStore = KeyStore.getInstance("PKCS12"),
   stream: InputStream = getClass.getClassLoader.getResourceAsStream("server.p12"),
   keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509"),
   tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509"),
   sslContext: SSLContext = SSLContext.getInstance("TLS")) {

  keyStore.load(stream, password)
  keyManagerFactory.init(keyStore, password)
  tmf.init(keyStore)

  def connectionContext(): HttpsConnectionContext = {
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    ConnectionContext.https(sslContext)
  }
}
