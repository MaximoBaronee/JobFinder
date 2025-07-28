from kivy.lang import Builder
from kivymd.app import MDApp
from kivymd.uix.list import OneLineListItem
from kivymd.uix.dialog import MDDialog
from kivymd.uix.button import MDFlatButton
from trabajos import buscar_trabajos   # Importar la función

KV = '''
MDScreen:
    MDBoxLayout:
        orientation: "vertical"
        MDTopAppBar:
            title: "Buscador de Empleos"
        MDTextField:
            id: search_field
            hint_text: "Palabra clave"
            pos_hint: {'center_x': 0.5}
            size_hint_x: 0.8
        MDTextField:
            id: location_field
            hint_text: "Ubicación"
            pos_hint: {'center_x': 0.5}
            size_hint_x: 0.8
        MDRaisedButton:
            text: "Buscar"
            pos_hint: {'center_x': 0.5}
            on_release: app.search_jobs()
        MDScrollView:
            MDList:
                id: job_list
'''

class JobFinderApp(MDApp):
    dialog = None

    def build(self):
        return Builder.load_string(KV)
    
    def show_dialog(self, text):
        """Muestra un mensaje emergente."""
        if not self.dialog:
            self.dialog = MDDialog(
                text=text,
                buttons=[MDFlatButton(text="OK", on_release=lambda x: self.dialog.dismiss())]
            )
        self.dialog.text = text
        self.dialog.open()

    def open_link(self, link):
        import webbrowser
        if link and link != "No disponible":
            webbrowser.open(link)
    
    def search_jobs(self):
        keyword = self.root.ids.search_field.text.strip()
        location = self.root.ids.location_field.text.strip()

        if not keyword or not location:
            self.show_dialog("Por favor, ingresa palabra clave y ubicación.")
            return

        job_list = self.root.ids.job_list
        job_list.clear_widgets()

        jobs = buscar_trabajos(keyword, location, max_offers=10)

        if not jobs:
            self.show_dialog("No se encontraron resultados para esa búsqueda.")
            return

        for job in jobs:
            item = OneLineListItem(
                text=f"{job['title']} - {job['company']}",
                on_release=lambda x, url=job['link']: self.open_link(url)
            )
            job_list.add_widget(item)

if __name__ == "__main__":
    JobFinderApp().run()
