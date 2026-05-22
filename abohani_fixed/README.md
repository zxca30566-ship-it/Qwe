# أبو هاني للخدمات الزراعية

تطبيق أندرويد لمتجر الخدمات الزراعية - مبني بـ Kotlin + Jetpack Compose + Firebase

## متطلبات التشغيل
- Android Studio Hedgehog (2023.1.1) أو أحدث
- JDK 17
- Android SDK 35

## كيفية تشغيل المشروع
1. افتح Android Studio
2. اختر "Open" وافتح مجلد المشروع
3. انتظر حتى ينتهي Gradle Sync تلقائياً
4. اتصل بالإنترنت (لتحميل Dependencies)
5. اضغط Run ▶

## بناء APK
- **Debug APK**: Build → Build Bundle(s)/APK(s) → Build APK(s)
- **Release APK**: Build → Generate Signed Bundle/APK

## الميزات الجديدة في هذا الإصدار
- ✅ رفع عدة صور من المعرض دفعة واحدة
- ✅ ضغط الصور تلقائياً قبل الرفع (لتحسين الأداء)
- ✅ مؤشر تحميل أثناء رفع الصور
- ✅ حذف صور محددة من القائمة
- ✅ تصحيح اتجاه الصورة تلقائياً
- ✅ دعم Android 14 (API 35)
- ✅ إصلاح updateCategory
- ✅ FileProvider للتوافق مع Android 10+
- ✅ تحديث Firebase BOM إلى 33.1.0

## هيكل المشروع
```
app/src/main/java/com/abuhani/agri/
├── MainActivity.kt
├── AboHaniApp.kt
├── data/
│   ├── local/         ← Room Database (offline cache)
│   ├── model/         ← Product, Category, CartItem
│   └── repository/    ← ProductRepository, CartRepository
├── ui/
│   ├── admin/         ← لوحة الأدمن
│   ├── cart/          ← سلة المشتريات
│   ├── category/      ← صفحة القسم
│   ├── home/          ← الصفحة الرئيسية
│   ├── navigation/    ← AppNavHost
│   ├── product/       ← تفاصيل المنتج
│   └── theme/         ← الألوان والثيم
└── utils/
    └── ImageUtils.kt  ← ضغط الصور وتصحيح الاتجاه
```

## الدخول للوحة الأدمن
اضغط طويلاً على اسم التطبيق في الشاشة الرئيسية
