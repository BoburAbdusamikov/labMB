# GitHub Users Retrofit App

## Exception When INTERNET Permission is Removed
When `<uses-permission android:name="android.permission.INTERNET" />` is removed from `AndroidManifest.xml`, the app throws:
`java.lang.SecurityException: Permission denied (missing INTERNET permission?)` (wrapped inside `java.net.UnknownHostException`).

---

## Demo Videos (Max 40s each)

<details>
<summary><b>1. Dependencies & Permission</b></summary>

*Video showing `build.gradle.kts` dependencies, `AndroidManifest.xml` permission, and successful Gradle sync.*
https://github.com/user-attachments/assets/placeholder-1

</details>

<details>
<summary><b>2. Model (@SerializedName & Misspelled Property)</b></summary>

*Video showing `avatarUrll` rendering `null` on screen without crashing, then fixed to `avatarUrl`.*
https://github.com/user-attachments/assets/placeholder-2

</details>

<details>
<summary><b>3. Base URL Trailing Slash Crash & Fix</b></summary>

*Video showing `IllegalArgumentException` in Logcat when trailing slash is missing, then fixed.*
https://github.com/user-attachments/assets/placeholder-3

</details>

<details>
<summary><b>4. Happy Path, Failure Path, Retry Exhaustion & Recovery</b></summary>

*Video showing: 1) Happy path loading 5 users & tapping a login to show ID/avatarUrl, 2) 404 debug button, 3) Airplane mode -> "No network" -> Retry 3 times -> "Open Wi-Fi settings" button appears, 4) Turning Wi-Fi back on and recovering list.*
https://github.com/user-attachments/assets/placeholder-4

</details>

<details>
<summary><b>5. ApiInstance Find in Files & OkHttp Logging (Optional Auth Header)</b></summary>

*Video showing single `Retrofit.Builder` occurrence via Find in Files and Logcat output with `Level.BASIC` / `Authorization` header.*
https://github.com/user-attachments/assets/placeholder-5

</details>

---

## How a Call Travels From Interface to Server

```text
GitHubService.users(perPage = 5)
        │
        ▼
[1. Retrofit Dynamic Proxy]
Retrofit intercepts the suspend function call via a dynamic proxy, reads @GET("/users") and @Query("per_page"), and constructs the HTTP request URL: https://api.github.com/users?per_page=5.
        │
        ▼
[2. OkHttp Client & Interceptors]
Retrofit hands the request to OkHttpClient. First, AuthInterceptor appends the "Authorization" header. Next, HttpLoggingInterceptor logs the outgoing request line and status/timing in debug builds (Level.BASIC) or stays silent in release (Level.NONE). OkHttp then opens the TLS socket to api.github.com and receives the raw JSON response body.
        │
        ▼
[3. GsonConverterFactory]
OkHttp passes the raw JSON stream back to Retrofit, which delegates deserialization to GsonConverterFactory. Gson maps JSON keys (e.g., "avatar_url" via @SerializedName to avatarUrl), ignores unmapped keys (html_url, type), and instantiates List<User>.
        │
        ▼
[4. safeApiCall & ViewModel StateFlow]
safeApiCall catches any IOException or HttpException and wraps the outcome into ApiResult, updating StateFlow<UiState> for Jetpack Compose to render.
```
