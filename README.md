# BudgetBru - Smart Budgeting for Students https://www.youtube.com/watch?v=nrToxu69IMI

<div align="center">

![Platform](https://img.shields.io/badge/platform-Android-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.7.0-blue)
![Room](https://img.shields.io/badge/Room-2.6.1-orange)
![License](https://img.shields.io/badge/license-MIT-red)

**A modern, feature-rich budget tracking app designed specifically for students to manage expenses, set financial goals, and track money owed to friends.**

[Features](#features) • [Screenshots](#screenshots) • [Tech Stack](#tech-stack) • [Installation](#installation) • [Usage](#usage) • [Demo Credentials](#demo-credentials)

</div>

---

## Overview

BudgetBru is a comprehensive budget tracking application built with modern Android technologies. It helps students track their spending, set monthly financial goals, manage IOUs with friends, and gain insights into their spending habits through detailed reports and visual analytics.

### Why BudgetBru?
- **Student-Focused**: Designed with student budgets and lifestyle in mind
- **Simple & Intuitive**: Clean, modern interface that's easy to navigate
- **Powerful Analytics**: Visual charts and reports to understand your spending
- **Full Data Control**: All data stored locally on your device

---

## Features

### Authentication
- Secure login system with username/password
- Demo account: `test` / `1234`

### Category Management
- Create custom expense categories
- Delete unwanted categories
- Pre-loaded default categories (Food, Transport, Groceries, etc.)

### Expense Tracking
- Add expenses with description, amount, and category
- Track start and end times for activities
- **Optional photo attachment** for receipts
- Quick add with preset amounts (R20, R50, R100, R200, R500)

### Financial Goals
- Set monthly income
- Define minimum and maximum spending goals
- Real-time progress tracking with visual indicators
- Daily, weekly, and monthly budget breakdowns

### Reports & Analytics
- **Date Range Selection**: View expenses for any period
- **Expense List**: All entries with photos accessible
- **Category Totals**: View total spent per category
- **Interactive Pie Chart**: Visual spending distribution
- **Progress Bars**: Percentage breakdown of spending

### IOU Tracker
- Track money lent to friends
- Track money borrowed from friends
- Mark IOUs as settled
- Filter by lent, borrowed, or all
- Summary cards showing totals

### Budgeting Tips
- 10+ expert budgeting tips for students
- Categories: Budgeting, Saving, Cutting Costs, Student Perks
- Search and filter functionality

### Settings
- Notification controls
- About and version info
- Share app feature
- Reset settings option

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 1.9.24 | Primary programming language |
| **Jetpack Compose** | 1.7.0 | Modern UI toolkit |
| **Material 3** | Latest | UI components and theming |
| **Room Database** | 2.6.1 | Local data persistence |
| **Navigation Compose** | 2.7.7 | Screen navigation |
| **Coroutines** | 1.8.1 | Async operations |
| **CameraX** | 1.3.4 | Camera integration |
| **Coil** | 2.5.0 | Image loading |
| **Gson** | 2.10.1 | JSON serialization |

### Architecture
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** for data management
- **Dependency Injection** (manual)
- **StateFlow** for reactive UI updates

---



## Installation

### Prerequisites
- Android Studio Flamingo (2022.2.1) or later
- JDK 11 or higher
- Android SDK 34 (Android 14)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/BudgetBru.git
   cd BudgetBru
