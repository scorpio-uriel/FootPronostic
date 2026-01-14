package com.example.footpronostic.ui.avatar

import android.content.Context
import coil.ImageLoader
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Configuration pour Coil permettant de contourner les erreurs de certificats SSL (Trust anchor not found).
 * Utile pour les environnements de test/académiques avec des émulateurs ou réseaux restreints.
 */
object CoilConfig {
    fun getImageLoader(context: Context): ImageLoader {
        val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0])
            .hostnameVerifier { _, _ -> true }
            .build()

        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .build()
    }
}
