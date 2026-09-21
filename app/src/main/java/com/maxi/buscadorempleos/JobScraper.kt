package com.maxi.buscadorempleos

import org.jsoup.Jsoup
import java.net.URLEncoder

/**
 * Representa una oferta de trabajo encontrada.
 * Equivalente al diccionario `job_info` de trabajos.py.
 */
data class JobOffer(
    val title: String,
    val company: String,
    val link: String
)

/**
 * Equivalente Kotlin de trabajos.py: busca ofertas en LinkedIn
 * a partir de una palabra clave y una ubicación.
 *
 * Debe llamarse desde un hilo de background (Dispatchers.IO),
 * nunca desde el hilo principal de Android.
 */
object JobScraper {

    private const val BASE_URL = "https://www.linkedin.com/jobs/search"
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"

    fun buscarTrabajos(
        keyword: String,
        location: String,
        maxOffers: Int = 20
    ): List<JobOffer> {
        val offers = mutableListOf<JobOffer>()

        val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
        val encodedLocation = URLEncoder.encode(location, "UTF-8")

        while (offers.size < maxOffers) {
            val start = offers.size
            val url = "$BASE_URL?keywords=$encodedKeyword&location=$encodedLocation&start=$start"

            val doc = try {
                Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10_000)
                    .get()
            } catch (e: Exception) {
                // Equivalente al "Error en la solicitud" del Python
                break
            }

            val results = doc.select("div.base-card")
            if (results.isEmpty()) {
                // No hay más resultados para esa búsqueda
                break
            }

            for (job in results) {
                if (offers.size >= maxOffers) break

                val title = job.selectFirst("h3.base-search-card__title")
                    ?.text()?.trim() ?: "No especificado"

                val company = job.selectFirst("h4.base-search-card__subtitle")
                    ?.text()?.trim() ?: "No especificado"

                val link = job.selectFirst("a.base-card__full-link")
                    ?.attr("href") ?: "No disponible"

                offers.add(JobOffer(title = title, company = company, link = link))
            }
        }

        return offers
    }
}
