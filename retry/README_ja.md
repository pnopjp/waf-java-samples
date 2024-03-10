# リトライサンプル

Microsoft Azure Well-Architected Framework に基づくクラウドデザインパターン実装編 (Java/Spring Boot版) のリトライサンプル集です。

## サンプル一覧

実行可能なサンプルは以下の通りです。

| サンプル名                                              | 概要                                                                                     |
| ------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| [01-general](./01-general/README.md)                    | 独自にJavaで実装したンプル                                                               |
| [02-httpcomponents](./02-httpcomponents/README.md)      | Apache HttpComponents による HTTPリクエストのリトライサンプル                            |
| [03-Resilience4j](./03-Resilience4J/README.md)          | Resilience4J Retry を利用したリトライサンプル                                            |
| [04-springboot](./04-springboot/README.md)              | Spring Retry と Spring Boot によるアノテーションやリトライテンプレートを利用したサンプル |
| [05-springboot-r4j](./05-springboot-with-r4j/README.md) | Spring Retry と Spring Boot によるアノテーションやリトライテンプレートを利用したサンプル |

## 参考リンク

* [再試行パターン - Cloud Design Patterns | Microsoft Docs](https://docs.microsoft.com/ja-jp/azure/architecture/patterns/retry)
* [再試行の一般的なガイダンス - Best practices for cloud applications | Microsoft Docs](https://docs.microsoft.com/ja-jp/azure/architecture/best-practices/transient-faults)
* [Azure サービスの再試行ガイダンス - Best practices for cloud applications | Microsoft Docs](https://docs.microsoft.com/ja-jp/azure/architecture/best-practices/retry-service-specific)
* [再試行ストームのアンチパターン - Performance antipatterns for cloud apps | Microsoft Docs](https://docs.microsoft.com/ja-jp/azure/architecture/antipatterns/retry-storm/)
  
以上


