import requests
from bs4 import BeautifulSoup

def buscar_trabajos(keyword, location, max_offers=20):
    base_url = "https://www.linkedin.com/jobs/search"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
    }

    params = {
        "keywords": keyword,
        "location": location,
        "start": 0
    }

    collected_offers = 0
    offers = []

    while collected_offers < max_offers:
        params["start"] = collected_offers
        response = requests.get(base_url, headers=headers, params=params)

        # Depuración: ver si LinkedIn responde correctamente
        print(f"URL consultada: {response.url} - Código: {response.status_code}")
        if response.status_code != 200:
            print("Error en la solicitud.")
            break

        soup = BeautifulSoup(response.content, "html.parser")
        results = soup.find_all("div", class_="base-card")

        if not results:
            # No hay resultados para esa búsqueda
            break

        for job in results:
            if collected_offers >= max_offers:
                break

            try:
                title_element = job.find("h3", class_="base-search-card__title")
                title = title_element.get_text(strip=True) if title_element else "No especificado"

                company_element = job.find("h4", class_="base-search-card__subtitle")
                company = company_element.get_text(strip=True) if company_element else "No especificado"

                link_element = job.find("a", class_="base-card__full-link")
                joblink = link_element["href"] if link_element else "No disponible"

                salary = "n/a"
                job_info = {
                    "title": title,
                    "company": company,
                    "salary": salary,
                    "link": joblink
                }
                offers.append(job_info)
                collected_offers += 1
            except Exception as e:
                print(f"Excepción: {e}")
                pass

    return offers

# Esto muestra un ejemplo de las 2 posibles salidas de la búsqueda
if __name__ == "__main__":
    resultados = buscar_trabajos("Programador Trainee", "Argentina")
    if resultados:
        for r in resultados:
            print(r)
    else:
        print("No se encontraron resultados para esa búsqueda.")


