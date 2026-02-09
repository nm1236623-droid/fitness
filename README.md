🏋️ FitnessApp｜Android 智慧健身紀錄應用

Android 原生應用，整合 AI 建議、雲端同步與訂閱制機制
狀態：Google Play 封閉測試（Closed Testing）

📌 專案摘要

FitnessApp 是一款以 實務可用性與系統完整度 為核心的 Android 原生健身紀錄應用。
本專案從需求規劃、架構設計、功能開發、雲端整合到上架封測，皆由本人獨立完成。

應用整合 Google Gemini AI 與 Firebase 雲端服務，提供使用者訓練紀錄管理、數據視覺化，以及依個人狀況動態生成的飲食與訓練建議。

🔧 核心技術與實作重點
AI 整合（Google Gemini）

串接 Google Gemini Pro API

根據使用者體重、訓練頻率與健身目標動態生成建議

實作 Prompt 結構設計與回傳資料解析，確保內容可被 App 穩定使用，而非單純文字顯示

Android 架構設計

採用 MVVM 架構

清楚區分 View / ViewModel / Repository

使用 Interface abstraction，降低模組耦合度，提升可維護性與擴充性

雲端與資料同步

Firebase Firestore

即時同步訓練紀錄與體重數據

使用使用者 UID 進行資料隔離，確保資料安全與隱私

Firebase Authentication

支援 Google Sign-In 與 Email 登入

完整處理登入狀態與 Session 管理

數據視覺化

使用 MPAndroidChart

將訓練量與體重變化轉換為折線圖與趨勢分析

提升使用者對長期健身進度的可讀性

商業化與實務經驗

訂閱制（RevenueCat）

區分免費與付費功能

熟悉 IAP 流程與訂閱狀態同步

廣告整合（AdMob）

實作廣告顯示與生命週期管理

📱 應用畫面（功能對應）
畫面	說明
登入 / 註冊	使用 Firebase Authentication，支援 Google 與 Email 登入，處理登入狀態與使用者 Session。
主畫面 / 儀表板	彙整當日訓練狀態、體重紀錄與功能入口，強調資訊可讀性與操作直覺。
AI 健身建議	串接 Google Gemini API，根據個人資料與歷史紀錄動態生成建議內容，非固定模板。
食物辨識 / 飲食紀錄	協助使用者快速記錄飲食內容，並與健身目標進行整合分析。
數據視覺化	使用 MPAndroidChart 呈現體重與訓練趨勢，輔助長期追蹤與調整。
畫面示意
登入	主畫面	AI 建議	食物辨識
<img src="https://github.com/user-attachments/assets/d021bb88-4b15-448e-88a9-38e41746dc3b" width="220"/>	<img src="https://github.com/user-attachments/assets/a560622a-4ac6-4a0f-b7a7-5bbd276336b0" width="220"/>	<img src="https://github.com/user-attachments/assets/735fce5f-9ad3-42ce-87b2-b83c61f81dc9" width="220"/>	<img src="https://github.com/user-attachments/assets/ea3f1fc2-f31d-4d08-beaa-0cca89184d0a" width="220"/>
🛠️ 技術堆疊

Language：Kotlin

IDE：Android Studio Ladybug

Architecture：MVVM / OOP

Backend：Firebase（Auth / Firestore / Storage）

AI：Google Gemini Pro API

Libraries

Retrofit / OkHttp（API 通訊）

Coil（圖片載入）

MPAndroidChart（圖表）

RevenueCat（訂閱制）

Version Control：Git / GitHub

👤 開發者

Name：Sid（陳佑軒）

Major：輔仁大學 軟體工程學系

Current Role：Network Engineer｜皇輝科技

Email：513210271@m365.fju.edu.tw
