Link Mockups: https://www.figma.com/design/bUcEfr3RnaMfPhft33jWym/Color-wheel-palette-generator--Comunidad-?node-id=0-1&t=EaEhoCP00xovlLTs-1

# Integrantes del grupo

1. **Aleshka Priscila Gamez Navidad**  
   **Carnet:** GN252409

2. **Gabriela Giselle Vásquez Rodriguez**  
   **Carnet:** VR250295

3. **Paola Matilde Orellana Castillo**  
   **Carnet:** OC250609

4. **Kleberson Rafael Mejia Perez**  
   **Carnet:** MP202140


# 📚 Mi Biblioteca Personal

Aplicación móvil nativa para Android desarrollada en Kotlin, orientada a la gestión de colecciones personales de lectura, control de inventario de ejemplares, préstamos con control de disponibilidad y sistema de reseñas con valoración.

---

## 🚀 Características Principales

* **Autenticación y Seguridad:** Registro e inicio de sesión mediante correo y contraseña gestionados a través de **Firebase Authentication**.
* **Gestión de Catálogo:** Consulta detallada de libros, filtrado, búsqueda dinámica y visualización de sinopsis y autores.
* **Sistema Transaccional de Préstamos:**
  * Solicitud de préstamos por 14 días con control de inventario en tiempo real.
  * Manejo de transacciones atómicas en Firestore para evitar inconsistencias de stock (`SIN_STOCK`).
  * Devolución de ejemplares y actualización automática de estados (`activo` / `devuelto`).
* **Reseñas y Calificaciones:** Módulo de opiniones con sistema de puntuación por estrellas, limitado a una reseña por usuario y libro.
* **Perfil de Usuario:** Seguimiento de libros leídos, historial de préstamos, gestión de reseñas personales y personalización básica local mediante `SharedPreferences`.

---

## 🛠️ Stack Tecnológico y Arquitectura

* **Lenguaje:** Kotlin
* **Plataforma:** Android (SDK nativo)
* **Entorno de Desarrollo:** Android Studio
* **Build System:** Gradle (Kotlin DSL - `.kts`)
* **Backend as a Service (BaaS):** 
  * **Firebase Authentication:** Manejo de sesiones y credenciales de usuario.
  * **Cloud Firestore:** Base de datos NoSQL documental para catálogos, préstamos y reseñas.
* **Almacenamiento Local:** `SharedPreferences` para datos de configuración rápida y perfil local.

---

## 🗄️ Modelo de Datos (Cloud Firestore)

El modelo está organizado en las siguientes colecciones principales:

* **`usuarios`**: Datos de cuenta (`uid`, `nombre`, `correo`, `fechaCreacion`).
* **`libros`**: Ficha técnica y existencias (`libroId`, `titulo`, `autor`, `genero`, `sinopsis`, `ejemplaresTotales`, `ejemplaresDisponibles`).
* **`prestamos`**: Registro de operaciones (`prestamoId`, `uid`, `libroId`, `titulo`, `autor`, `estado`, `fechaPrestamo`, `fechaDevolucionPrevista`, `fechaDevolucion`).
* **`resenas`**: Valoraciones de la comunidad (`resenaId = uid_libroId`, `uid`, `libroId`, `titulo`, `usuario`, `puntuacion`, `comentario`, `fecha`).

---

## 📱 Mapa de Pantallas

* **Autenticación:** `LoginActivity`, `RegisterActivity`.
* **Exploración:** `MainActivity`, `SearchActivity`, `BookDetailActivity`.
* **Préstamos y Reseñas:** `LoansActivity`, `ReviewsActivity`, `MyReviewsActivity`.
* **Perfil y Notificaciones:** `ProfileActivity`, `EditProfileActivity`, `ReadBooksActivity`, `NotificationsActivity`.

---

## 📁 Estructura del Repositorio

```text
├── app/                  # Código fuente de la aplicación Android (Activities, Fragments, Utils)
├── gradle/               # Wrapper y configuración de Gradle
├── manuales/             # Manuales técnicos y documentación formal del proyecto
├── build.gradle.kts      # Script de compilación raíz
├── settings.gradle.kts   # Configuración de repositorios y módulos
└── README.md             # Documentación general del repositorio
