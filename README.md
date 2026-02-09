# 🏋️ FitnessApp｜智能健身教練與紀錄平台

> **Android Native • Google Gemini AI • Cloud Sync • Subscription Model**
> **狀態：Google Play 封閉測試中 (Closed Testing)**

---

## 📌 專案概述 (Project Summary)

**FitnessApp** 是一款以實務商用標準開發的 Android 原生應用，整合 **Google Gemini Pro AI** 與 **Firebase** 雲端服務。

本專案由本人獨立完成從需求分析、架構設計、全端開發到上架測試的完整週期。不同於一般的紀錄工具，FitnessApp 透過生成式 AI 提供「個人化教練建議」，並整合 **RevenueCat** 訂閱機制與 **AdMob** 廣告，展示了從技術實作到商業變現的完整解決方案。

---

## 🚀 核心技術亮點 (Technical Highlights)

### 1. 生成式 AI 應用 (Gen-AI Integration)
* **動態 Prompt 工程**：設計結構化 Prompt，將使用者生理數據（體重、BMI、目標）轉換為精準的 AI 建議，確保回應內容可被 App 解析並視覺化呈現。
* **情境感知**：串接 **Google Gemini Pro API**，提供即時的飲食營養分析與訓練強度調整建議。

### 2. 現代化 Android 架構 (Modern Android Architecture)
* **MVVM & Clean Architecture**：嚴格遵守關注點分離原則 (SoC)，透過 ViewModel 處理商業邏輯，Repository 管理資料來源。
* **介面抽象化 (Interface Abstraction)**：降低模組耦合度 (Coupling)，提升單元測試的可測試性與程式碼維護性。

### 3. 雲端架構與資料安全 (Cloud & Security)
* **Firebase Firestore**：實作 NoSQL 資料庫結構設計，支援離線快取與即時多裝置同步。
* **Identity Management**：整合 Firebase Auth (Google/Email)，並實作完整的使用者 Session 管理與資料隔離機制 (UID-based Security Rules)。

### 4. 商業化整合 (Monetization)
* **混合變現模式**：整合 **RevenueCat** 實作 IAP 訂閱制 (Free/Premium 分級)，並串接 **Google AdMob** 進行廣告流量變現，具備實際商業模組開發經驗。
* **數據視覺化**：客製化 **MPAndroidChart**，將抽象的訓練數據轉化為可互動的趨勢圖表，提升用戶留存率。

---

## 📱 應用程式畫面 (App Screenshots)

| 登入 / 註冊 | 主畫面 / 儀表板 | AI 健身建議 | 食物辨識 / 紀錄 |
|:---:|:---:|:---:|:---:|
| <img width="200" height="400" alt="Image" src="https://github.com/user-attachments/assets/7542cf44-b195-4ab5-9c1d-77f89213a2de" /> | <img width="200" height="400" alt="Image" src="https://github.com/user-attachments/assets/c64b15a4-fc32-49db-ae2e-47d3bd8d3a55" /> | <img width="200" height="400" alt="Image" src="https://github.com/user-attachments/assets/43c13f67-1334-4168-b21f-827f8f3d3145" /> |<img width="300" height="600" alt="Image" src="https://github.com/user-attachments/assets/b5ea56fa-55fe-4ddf-83ce-0222c530c313" />|
| **Firebase Auth**<br>Google / Email 登入 | **Dashboard**<br>數據視覺化與狀態總覽 | **Gemini AI**<br>動態生成個人化建議 | **Diet Tracking**<br>AI 輔助熱量估算 |

---

## 🛠️ 技術堆疊 (Tech Stack)

* **Language:** Kotlin
* **IDE:** Android Studio Ladybug
* **Architecture:** MVVM, Repository Pattern, OOP
* **AI Core:** Google Gemini Pro API
* **Backend as a Service:** Firebase (Auth, Firestore, Storage)
* **Libraries:**
    * *Networking:* Retrofit, OkHttp
    * *UI/UX:* MPAndroidChart, Coil
    * *Monetization:* RevenueCat (Subscription), Google AdMob
* **Tools:** Git, GitHub, Postman

---

## 👤 開發者資訊 (Developer)

* **Name:** Sid Chen (陳佑軒)
* **Major:** 輔仁大學 軟體工程與數位創意學士學位學程
* **Current Role:** Network Engineer｜皇輝科技
* **Email:** 513210271@m365.fju.edu.tw
