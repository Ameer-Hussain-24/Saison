# Saison Release 签名配置指南

## 配置完成

Release 签名已成功配置!现在你可以构建已签名的 release APK。

## Keystore 信息

- **文件位置**: `saison-release.jks` (项目根目录)
- **Store Password**: `saison2024`
- **Key Alias**: `saison-key`
- **Key Password**: `saison2024`
- **有效期**: 10000 天

## 构建 Release APK

运行以下命令构建已签名的 release APK:

```bash
.\gradlew assembleRelease
```

生成的 APK 位置:
```
app/build/outputs/apk/release/app-release.apk
```

## 安全提示

⚠️ **重要**: 
- keystore 文件 (`*.jks`) 已添加到 `.gitignore`,不会被提交到版本控制
- 请妥善保管 keystore 文件和密码
- 如果丢失 keystore,将无法更新已发布的应用
- 建议将 keystore 文件备份到安全位置

## 生产环境建议

对于生产环境,建议:

1. 使用更强的密码
2. 将密码存储在环境变量或 `local.properties` 中
3. 不要在代码中硬编码密码

示例配置 (使用环境变量):

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../saison-release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

## 验证签名

验证 APK 是否已正确签名:

```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

## 下一步

现在你可以:
1. 安装 release APK 到设备进行测试
2. 上传到 Google Play Console
3. 分发给测试用户

构建成功! 🎉
