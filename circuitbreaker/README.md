# サーキットブレーカーサンプル

Microsoft Azure Well-Architected Framework に基づくクラウドデザインパターン実装編 (Java/Spring Boot編) のサーキットブレーカーサンプル集です。

## サンプル一覧

実行可能なサンプルは以下の通りです。

| サンプル名 | 概要 |
|---|---|
| [01-general-b](./01-general-cb/README.md)| 独自にJavaで実装したサンプル  |
| [02-rejilience4j](./02-resilience4j-cb/README.md)| Resilience4J Retry を利用したサーキットブレーカーサンプル |
| [03-springboot-cb](./03-springboot-cb/README.md)         | Spring Boot + Spring Cloud Circuit Breaker + Spring Retry を利用したサンプル |
| [04-springboot-cb-r4j](./04-springboot-cb-r4j/README.md) | Spring Boot + Spring Cloud Circuit Breaker + Ressilience4j  を利用したサンプル |

## 参考リンク

* [サーキット ブレーカー パターン - Cloud Design Patterns | Microsoft Docs](https://docs.microsoft.com/ja-jp/azure/architecture/patterns/circuit-breaker)
* [サーキット ブレーカー パターンの実装 | Microsoft Docs](https://docs.microsoft.com/ja-jp/dotnet/architecture/microservices/implement-resilient-applications/implement-circuit-breaker-pattern)

以上

