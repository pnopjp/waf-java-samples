# Microsoft Azure Well-Architected Framework Samples for Java

### 更新 2024/03

- 更新 依存ライブラリ
- 更新 Spring Boot Version 2.7.9 から 3.2.3
- 英語ドキュメントの追加

### 更新 2023/03

- 更新 依存ライブラリ
- 更新 Java verion 11 から 17.
- 更新 Spring Boot Version 2.5.x から 2.7.9
- 英語ドキュメントの用意（TOPのみ、今後対応）

## イントロ
Microsoft Azure Well-Architected Framework に基づくクラウドデザインパターン実装編 (Java/Spring Boot版) セミナーの内容に基づいて、以下の実行可能なサンプルを提供しています。

| フォルダ                                                             | サンプル内容                       |
| -------------------------------------------------------------------- | ---------------------------------- |
| [retry](./retry/README.md)                                           | リトライ（再試行）パターン         |
| [circuitbreaker](./circuitbreaker/README.md)                         | サーキットブレーカー パターン      |
| [health-endpoint-monitoring](./health-endpoint-monitoring/README.md) | 正常性エンドポイントの監視パターン |
| [queue-base-load-leveling](./queue-base-load-leveling/README.md)     | キュー ベースの負荷平準化パターン  |
| [asynchronous-request-reply](./asynchronous-request-reply/README.md) | 非同期応答パターン                 |

## 前提条件

実行するにあたって以下の前提条件があります。サンプルの実行方法は個々の `README.md` に書かれています。

- Java 17 以降
- [Maven 3.8](https://maven.apache.org/index.html) 以降
- curl コマンド（一部サンプル）
- [Azure Storge エミュレータ](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-emulator) または [Azurite エミュレータ](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-azurite?tabs=npm) （一部サンプル）
- [Azure Functions Core ツール](https://docs.microsoft.com/ja-jp/azure/azure-functions/functions-run-local)（一部サンプル）
- Docker / Docker Compose （一部サンプル、なくても実行はできますが環境構築が簡単になります）
 
## 利用する外部サービス

任意のステータスコードを返す Web サービスを、「外部サービス」として見立てて利用しているサンプルがあります。この Web サービスでは、リクエストに応じたHTTP ステータスコードを返却したり、遅延したレスポンスを返却することができます。

* [httpbin.org](http://httpbin.org/)

## サンプル動作確認環境

本サンプルは以下の環境で動作確認しました。

- Windows 11 WSL2 環境上の Ubuntu20.04
- OpenJDK Runtime Environment Microsoft-32931 (build 17.0.3+7-LTS)
- Apache Maven 3.8.3
- Eclipse IDE for Java Developers (2021-3)
- Visual Studio Code 1.61.0 
- Docker Desktop fo Windows v20.10.8

## 利用条件

- 本サンプルに記載されている情報は、本サンプル作成時点での情報となります。
- 状況等の変化により内容は変更される場合があります。
- 本サンプルの使用に関連してお客様、お客様の関連会社、または第三者に生ずる間接的、付随的、結果的な損害（営業機会や営業情報の損失などを含む）について一切責任を負いません。

## ライセンス

本サンプルは [MIT ライセンス](./LICENSE.txt)下で公開されています。

以上