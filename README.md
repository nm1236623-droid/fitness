# 🏋️ FitnessApp｜你的 AI 智能健身教練

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7f52ff?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%20Pro-4285F4?logo=google&logoColor=white)](https://deepmind.google/technologies/gemini/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Google Play](https://img.shields.io/badge/Get_it_on-Google_Play-000000?logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=tw.edu.fju.myfitnessapp)

> **這不只是一個紀錄工具，而是一個住在手機裡的私人教練。**
> 結合 **Google Gemini AI** 與完整商業訂閱模型，讓你的健身數據轉化為真正的行動建議。

---

## 📱 應用程式畫面 (App Screenshots)

| 🔐 簡單登入 | 📊 數據一看就懂 | 🤖 AI 智能分析 | 🥗 飲食熱量追蹤 |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/7542cf44-b195-4ab5-9c1d-77f89213a2de" width="180"> | <img src="https://github.com/user-attachments/assets/c64b15a4-fc32-49db-ae2e-47d3bd8d3a55" width="180"> | <img src="https://github.com/user-attachments/assets/43c13f67-1334-4168-b21f-827f8f3d3145" width="180"> | <img src="https://github.com/user-attachments/assets/b5ea56fa-55fe-4ddf-83ce-0222c530c313" width="180"> |
| **Firebase Auth**<br>Google 一鍵登入 | **Dashboard**<br>進步清晰可見 | **Gemini AI**<br>專屬個人化建議 | **Diet Tracking**<br>AI 輔助熱量估算 |

*(全新改版：快捷選單支援「有氧運動」、「InBody」、「體態相簿」與最新的「健身爬蟲」功能)*

---

## ✨ 專案亮點 (Key Highlights)

*   **🤖 AI 賦能教練**：透過 Prompt Engineering 串接 **Gemini Pro**，根據使用者的 BMI、目標與飲食紀錄，提供主動式的訓練建議，不再只是冷冰冰的數字。
*   **🕷️ 健身資訊爬蟲 (New!)**：內建網路爬蟲引擎，讓使用者能快速獲取、統整外部的健身新知與相關數據，持續擴充知識庫。
*   **☁️ 雲端即時同步**：整合 **Firebase (Auth/Firestore/Storage)**，確保健身進度與體態紀錄在不同裝置間無縫接軌。
*   **💰 完整的商業模型**：實作 **RevenueCat** 訂閱機制與 AdMob 廣告變現，完整模擬真實產品的營運邏輯。
*   **🏗️ 現代化架構**：嚴謹遵守 **MVVM** 與 **Clean Architecture**，確保程式碼具備高可測試性與維護性。

---

## 🛠️ 技術堆疊 (Tech Stack)

*   **開發語言**：Kotlin (Coroutines, Flow)
*   **架構與核心**：MVVM, Repository Pattern, ViewModel
*   **後端服務**：Firebase (Authentication, Firestore, Storage)
*   **AI 整合**：Google Gemini Pro API (via Retrofit)
*   **商業邏輯**：RevenueCat (IAP), Google AdMob
*   **數據與功能**：MPAndroidChart (數據視覺化), Web Scraping (爬蟲技術)

---

## 🚀 如何啟動與執行

1.  **Clone 專案**：
    ```bash
    git clone [https://github.com/nm1236623-droid/FitnessApp.git](https://github.com/nm1236623-droid/FitnessApp.git)

    開啟專案：使用 Android Studio 開啟。

⚠️ 注意事項：本專案依賴 local.properties 中的 API Keys (Gemini, Firebase 等)。如需獲取測試憑證，歡迎聯絡開發者。

👨‍💻 關於開發者
背景：具備網路工程師經驗，目前於輔仁大學進修軟體工程。

專長：Android App 開發、API 架構設計、系統整合。

聯絡方式：513210271@cloud.fju.edu.tw

🎉 本應用程式已正式於 Google Play 上架！
🎉 **本應用程式已正式於 Google Play 上架！**
[👉 點我前往 Google Play 下載 FitnessApp](https://play.google.com/store/apps/details?id=tw.edu.fju.myfitnessapp&pli=1)
