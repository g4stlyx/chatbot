# Backend

### part 1 (8. hafta istenen rapor için)

- güvenlik konfigürasyonları tamamlandı.
  - CORS (dev. için sadece localhost, domain'e bağlanınca ayarlanabilir.)
  - public endpointler ayarlandı, kalanlar JWT tokeni ve auth gerektiriyor.
- temel konfigürasyonlar tamamlandı.
  - local'e ollama (llama:7b) modeli kuruldu, backend ile bağlantısı ve konfigürasyonları ayarlandı.
  - local'e docker aracılığıyla redis kuruldu, bağlantısı ve konfigürasyonları ayarlandı.
  - local'e mysql db kuruldu.
- tüm modeller hazırlandı ve db yapısı tamamlandı.
- şifre saklama, giriş, kayıt olma, şifre sıfırlama, email doğrulama işlemleri tamamlandı.
  - argon2 ile hashleme, salt ve pepper kullanarak şifre saklama
  - jwt token ile authentication sağlama
  - giriş ve kayıt olma işlemleri için fonksiyonlar ve endpointler.
  - email doğrulama ve parola sıfırlama işlemleri için fonksiyonlar, endpointler, mail işlemleri.
- admin ve kullanıcılar için profil işlemleri. servis fonksiyonları ve endpointler. hesap kapatma/açma işlemleri.
- kullanıcılar için chat session yönetimi (CRUD, pause session, activate session, archive session)
- kullanıcılar için chat mesaj yönetimi (creating a new session with a message and writing to an existing one)
  - get message history, get single message
  - edit message (2 endpoints, 1 for with regenerating the response, 1 for not doing it)
  - regenerate last response
  - streaming and non-streaming options

### part 2 (son rapor için, uygulamanın full hali)

- tüm admin panel işlemleri
  - kullanıcı yönetimi (CRUD)
  - admin yönetimi (level 0 adminler için) (CRUD)
  - admin aktivitesi loglama, ve bu logları level 0 adminlerin görüntüleyebilmesi için endpointler. (read-only)
  - chat session'larının, mesajların yönetimi (CRUD)
  - email doğrulama ve şifre sıfırlama tokenlerini görüntülenmesi (read-only)
- rate limiting için servis ve konfigürasyonlar ayarlandı.
- çeşitli (redis, ollama vs.) health check eden servisler kuruldu.
- log the auth. errors like 403 or 401 (or even 404s). who tried (if req. has a token), ip, etc. info (again with async processing)
- 🔄 chat sharing? (is_public) - Basic support added, needs testing
  - user a accessing user b's private chat should be tested too
- projects kısmı, chatleri gruplandırmak için (gptdeki gibi)
- hazır prompt şablonları, kullanıcı ekleyebilir veya admin panelden yönetilecek şekilde olabilir (tuğberk hocanın repodaki gibi)
  - gemini'daki gem'ler tarzı bir şey olabilir
    - ismi, açıklaması, system promptu (talimatları) var. bunun üstüne prompt giriliyor.
- mobil?

# Frontend

### part 1 (8. hafta istenen rapor için)

- temel UI konfigürasyonları tamamlandı.
  - React + Vite ile modern frontend yapısı kuruldu.
  - React Router ile sayfa yönlendirmeleri yapılandırıldı.
  - Axios ile HTTP istemci ve interceptor'lar ayarlandı (JWT token otomatik ekleme, 401 hata yönetimi).
- Context API ile state yönetimi.
  - AuthContext: kullanıcı authentication state'i, login/logout/register işlemleri.
  - ChatContext: chat session'ları, mesajlar ve chat işlemleri için global state yönetimi.
- kimlik doğrulama sayfaları ve özellikleri tamamlandı.
  - giriş yapma (LoginPage) ve kayıt olma (RegisterPage) sayfaları.
  - email doğrulama sayfası (EmailVerifyPage) ve bekleyen doğrulama sayfası (VerificationPendingPage).
  - şifre sıfırlama sayfaları (ForgotPasswordPage, ResetPasswordPage).
  - email doğrulama yeniden gönderme özelliği.
  - ProtectedRoute bileşeni ile korumalı sayfa yönlendirmeleri.
- profil yönetimi özellikleri.
  - profil görüntüleme ve düzenleme (ProfilePage).
  - kullanıcı bilgilerini güncelleme (email, ad, soyad, profil resmi).
  - şifre değiştirme özelliği.
  - hesap deaktive etme/reaktive etme.
- chat session yönetimi.
  - session listesi görüntüleme ve filtreleme (status: ACTIVE, PAUSED, ARCHIVED).
  - yeni session oluşturma.
  - session seçme ve geçiş yapma.
  - session silme.
  - session yeniden adlandırma.
  - session durum yönetimi (pause, activate, archive).
- chat mesajlaşma özellikleri.
  - mesaj gönderme (streaming ve non-streaming).
  - mesaj geçmişi görüntüleme.
  - real-time streaming yanıtlar (SSE - Server-Sent Events).
  - mesajları düzenleme (EditMessageModal ile).
  - düzenleme sırasında AI yanıtını yeniden oluşturma seçeneği.
  - mesaj silme.
  - AI yanıtını yeniden oluşturma (regenerate).
  - mesaj işlemleri için hover menüler (MessageActions).
- UI/UX bileşenleri.
  - Sidebar: session listesi, kullanıcı bilgisi, durum filtreleri.
  - ChatWindow: aktif konuşma arayüzü.
  - MessageList: mesaj geçmişi, markdown ve kod syntax highlighting desteği.
  - MessageInput: mesaj girişi, streaming desteği.
  - SessionActions: session işlemleri için dropdown menü.
  - Markdown rendering (react-markdown + remark-gfm).
  - Code syntax highlighting (react-syntax-highlighter).

### part 2 (son rapor için, uygulamanın full hali)

- (frontend'de admin panel özellikleri henüz implement edilmedi)
- gelişmiş chat özellikleri
  - streaming yanıtları durdurma özelliği
  - mesaj düzenleme modalı ile gelişmiş düzenleme deneyimi
  - mesaj zamanı gösterimi (date-fns ile)
  - düzenlenmiş mesajlar için gösterge (edited badge)
- session yönetimi iyileştirmeleri
  - session durum badge'leri (aktif/durdurulmuş/arşivlenmiş)
  - inline session yeniden adlandırma (klavye kısayolları ile)
  - session işlemleri için context menu
- hata yönetimi ve kullanıcı geri bildirimleri
  - email doğrulama hatalarını özel gösterge ile belirtme
  - başarılı/başarısız işlemler için bildirimler
  - yükleme durumları ve disable state'ler

### yapılacak özellikler (part 2'ye eklenebilir)

- admin panel arayüzü
  - kullanıcı yönetimi sayfası
  - admin yönetimi sayfası
  - aktivite logları görüntüleme
  - chat session ve mesaj yönetimi
  - token yönetimi görüntüleme
- rate limiting göstergeleri
- gelişmiş profil özellikleri (avatar yükleme vb.)
- chat paylaşma özelliği (public/private toggle)
- projeler/kategoriler ile chat gruplama
- hazır prompt şablonları
- dark/light tema desteği
- mobil responsive iyileştirmeler
