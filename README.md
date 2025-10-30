[README.md](https://github.com/user-attachments/files/23245381/README.md)
# 🇹🇷 WTTR Android Uygulaması

**WTTR (War Thunder Türkiye Resmî Mobil Uygulaması)**  
Flarum tabanlı [warthunder.tr](https://warthunder.tr) forumuna erişim sağlayan, tamamen yerli geliştirilmiş mobil uygulamadır.  
Kullanıcılar mobil cihazlarından rahatlıkla gezinebilir, yeni içerikleri takip edebilir ve **Neon Chat** üzerinden anlık mesajlaşabilir.

---

## 🚀 Özellikler

- 🌐 Tam entegre **WebView** tabanlı gezinti  
- 💬 **Flarum Neon Chat** desteği (canlı sohbet, medya paylaşımı)  
- 🔔 **Yeni içerik bildirimi** (Atom feed ile 30 dakikada bir kontrol)  
- 📰 **Bildirim tıklama → gönderiyi uygulamada açma (deep link)**  
- 📷 **Dosya yükleme, galeri ve kamera entegrasyonu**  
- 🔄 **Çek–yenile (Pull to refresh)** desteği  
- 🎨 **WTTR temalı splash ekranı ve modern koyu tema arayüzü**  
- 🧱 **Offline hata sayfası + geri tuşu desteği**

---

## 🛠️ Kurulum ve Derleme

### Gereksinimler
- Android Studio (Giraffe veya üzeri)
- Gradle 8+
- JDK 17
- Android SDK 34

### Derleme
```bash
git clone https://github.com/<kullanıcı-adın>/wttr-android.git
cd wttr-android
./gradlew assembleDebug
```

APK dosyası şu dizinde oluşur:  
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔐 İmzalı (Release) Derleme

1. `keystore.properties` dosyasını oluştur:
   ```properties
   storeFile=wttr-release.keystore
   storePassword=ŞİFREN
   keyAlias=wttr_key
   keyPassword=ANAHTAR_ŞİFREN
   ```
2. Android Studio → **Build ▸ Generate Signed Bundle / APK**
3. `app-release.apk` veya `app-release.aab` dosyasını al.

---

## 🤖 GitHub Actions Build

Bu proje, her push işleminde otomatik olarak derlenir.

- **Debug build:** `wttr-app-debug-apk`
- **Release build:** `wttr-release-artifacts` (imzalı APK + AAB)

Artifacts, GitHub Actions sekmesinden indirilebilir.

---

## ⚙️ Teknolojiler

- **Kotlin (Android 13+)**
- **Flarum API + Atom Feed**
- **Neon Chat Entegrasyonu**
- **Firebase (isteğe bağlı Push bildirim desteği)**

---

## 📱 Ekran Görselleri

_(Eklenecek: Splash ekranı, forum ana sayfası, Neon Chat, bildirim örneği)_

---

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) altında dağıtılmaktadır.  
© 2025 **WTTR • War Thunder Türkiye**

---

> 💡 **Not:** Flarum Neon Chat eklentisi WebSocket tabanlı çalışır.  
> Android WebView bunu destekler; dolayısıyla canlı sohbet tam uyumlu şekilde çalışmaktadır.
