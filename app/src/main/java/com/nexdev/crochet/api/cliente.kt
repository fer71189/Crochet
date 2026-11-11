package com.nexdev.crochet.api

import com.nexdev.crochet.api.model.CatalogoResponse
import com.nexdev.crochet.api.model.Patron
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Define las rutas (endpoints) de tu servidor de C#
interface AmigurumiApiService {
    @GET("api/patrones")
    suspend fun getCatalogo(): CatalogoResponse

    // Enviar un patrón a la "sala de espera" del servidor
    @POST("api/patrones/proponer")
    suspend fun proponerPatron(@Body nuevoPatron: Patron)
}

// Objeto Singleton para instanciar el cliente una sola vez
object RetrofitClient {
    private const val BASE_URL = "http://67.215.228.198/"

    val instance: AmigurumiApiService by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        retrofit.create(AmigurumiApiService::class.java)
    }
}