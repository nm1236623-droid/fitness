# 🏋️ FitnessApp｜智能健身教練與紀錄平台

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7f52ff?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%20Pro-4285F4?logo=google&logoColor=white)](https://deepmind.google/technologies/gemini/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Status](https://img.shields.io/badge/Status-Closed%20Testing-orange)]()

> **結合 Google Gemini 生成式 AI 的個人化健身教練，具備完整的商業訂閱模型與雲端同步功能。**

---

## 📖 專案簡介 (Overview)

**FitnessApp** 是一款以實務商用標準開發的 Android 原生應用。有別於傳統的紀錄工具，本專案整合了 **Generative AI (Gen-AI)** 技術，能夠根據用戶的生理數據（體重、BMI、飲食紀錄）提供即時、個人化的訓練建議。

這不只是一個 Side Project，而是一個完整的產品實踐——從 **MVVM 架構設計**、**Firebase 雲端整合**、到 **RevenueCat 訂閱制**與 **Google AdMob 廣告變現**，完整展示了軟體開發生命週期 (SDLC) 的執行能力。

### 核心價值
* **🤖 AI 賦能**：利用 Prompt Engineering 串接 Gemini Pro，將被動數據轉化為依據情境的主動建議。
* **☁️ 雲端同步**：透過 Firestore 實現跨裝置資料即時同步。
* **💰 商業變現**：實作 IAP (應用程式內購) 訂閱機制與廣告模組，驗證商業模式。

---

## 📱 應用程式畫面 (App Screenshots)

| 🔐 身份驗證 & 安全 | 📊 數據儀表板 | 🤖 AI 智能分析 | 🥗 飲食熱量追蹤 |
|:---:|:---:|:---:|:---:|
| <img width="200" height="400" alt="Login" src="https://github.com/user-attachments/assets/7542cf44-b195-4ab5-9c1d-77f89213a2de" /> | <img width="200" height="400" alt="Dashboard" src="https://github.com/user-attachments/assets/c64b15a4-fc32-49db-ae2e-47d3bd8d3a55" /> | <img width="200" height="400" alt="AI Advice" src="https://github.com/user-attachments/assets/43c13f67-1334-4168-b21f-827f8f3d3145" /> | <img width="200" height="400" alt="Diet Tracking" src="https://github.com/user-attachments/assets/b5ea56fa-55fe-4ddf-83ce-0222c530c313" /> |
| **Firebase Auth**<br>Google / Email 登入 | **Dashboard**<br>數據視覺化與狀態總覽 | **Gemini AI**<br>動態生成個人化建議 | **Diet Tracking**<br>AI 輔助熱量估算 |

---

## 🛠️ 技術堆疊 (Tech Stack)

### 🏗️ 架構與核心 (Architecture & Core)
* **Language:** Kotlin (Coroutines, Flow)
* **Architecture:** MVVM (Model-View-ViewModel), Clean Architecture Principles
* **UI Toolkit:** XML Layouts, Material Design Components

### ☁️ 雲端與後端 (Cloud & Backend)
* **BaaS:** Firebase (Auth, Firestore NoSQL Database, Storage)
* **AI Integration:** Google Gemini Pro API (via Retrofit)
* **Networking:** Retrofit2, OkHttp3, GSON

### 💼 商業邏輯與工具 (Business & Tools)
* **Monetization:** RevenueCat (Subscription Management), Google AdMob
* **Visualization:** MPAndroidChart (Customized Charts)
* **Image Loading:** Coil
* **Version Control:** Git, GitHub Flow

---

## 💡 關鍵功能實作 (Key Features)

### 1. 生成式 AI 教練 (Gen-AI Integration)
* 設計結構化的 **System Prompts**，將用戶的 BMI、目標體重與昨日攝取熱量打包發送至 Gemini API。
* 解析 AI 回傳的 Markdown 格式建議，並將其視覺化呈現於 UI，解決 API 回應不穩定的問題。

### 2. 現代化 Android 架構
* 嚴格遵守 **關注點分離 (Separation of Concerns)**。
* **ViewModel** 處理商業邏輯與 UI 狀態持有，**Repository** 負責統整資料來源 (Remote/Local)，確保程式碼的可測試性與維護性。

### 3. 混合變現模式 (Hybrid Monetization)
* 整合 **RevenueCat** SDK，處理複雜的訂閱狀態（試用期、續訂、過期）。
* 實作 **Feature Gating (功能閘道)**，區分免費用戶與付費會員 (Premium) 的權限差異。

---

## 🚀 如何執行 (How to Run)

1.  Clone 此專案到本地：
    ```bash
    git clone [https://github.com/YOUR_USERNAME/FitnessApp.git](https://github.com/YOUR_USERNAME/FitnessApp.git)
    ```
2.  在 Android Studio 中開啟專案。
3.  **注意**：本專案依賴 `local.properties` 中的 API Keys (Gemini API, Firebase Config)。若需編譯執行，請聯繫開發者獲取測試憑證。

---

## 👨‍💻 開發者 (Developer)

**Sid Chen (陳佑軒)**
* **Role:** Android Developer (Independent)
* **Background:** 具備網路工程師背景，熟悉 API 串接與系統架構；目前於輔仁大學進修軟體工程學位。
* **Contact:** [Email](mailto:513210271@m365.fju.edu.tw)

> *此專案目前處於 Google Play 封閉測試階段 (Closed Testing Track)。*
