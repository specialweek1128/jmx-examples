# JMXログイン監視サンプル

JMXを使ってログイン数やログインIDの監視を行えるサンプル。

## 機能概要

jconsoleでログイン情報を保持するMBeanを操作することができる。
また、自作の監視アプリではヒープの情報やログイン情報を標準出力に出力することができる。

## 機能詳細

### Webアプリケーション

SpringBootで作成したログインするだけのWebアプリケーションが実装されている。
このアプリケーションの機能は以下の通り。

- ログイン機能
  - 100から300までのIDでログインすることができる。
  - ログイン時のパスワードは`testtest`となっている。
- ログイン人数制限
  - 指定したログイン上限を超えるとログインエラーとなる機能。
- IDロック
  - 指定したIDをロックすることができる。

### LoginMonitorMBean

LoginMonitorMBeanではログイン情報を保持している。
この値はJMXでデータを管理している。
監視項目としては以下の通り。

| メソッド名 | 機能説明 |
| --- | --- |
| getLoginCount | ログイン数が取得できる。 |
| getLoginInfos | ログインしているユーザのIDと名称が取得できる。 |
| addLoginInfo | ログイン情報を追加することができる。 |
| removeLoginInfo | 指定されたIDのログイン情報を削除する。 |
| resetLoginInfo | ログイン情報をリセットする。 |
| getMaxLoginCount | 最大ログイン数を取得できる。 |
| setMaxLoginCount | 最大ログイン数を設定できる。 |
| getLoginLockIds | ロックしているIDの配列を取得できる。 |
| addLoginLockId | ロックするIDを追加することができる。 |
| removeLoginLockId | ロックしているIDを削除する。 |
| resetLoginLockId | ロックしているIDをリセットする。 |

### ProcessMonitor

JMXを使用してヒープ情報やログイン情報を表示しるクライアントアプリケーション。
このアプリケーションのUsageは以下の通り。

```
usage: java examples.jmx.ProcessMonitor [option]
 -?,--help             ヘルプメッセージを表示します。
 -d,--dispName <arg>   表示名を指定してください。この値を指定するとローカルプロセスに接続を行います。
 -h,--host <arg>       ホスト名を指定してください。この値デフォルト値は"localhost"です。
 -i,--pid <arg>        プロセスIDを指定してください。この値を指定するとローカルプロセスに接続を行います。
 -p,--port <arg>       ポート番号を指定してください。この値デフォルト値は"5000"です。
 ```

このクライアントアプリケーションで別プロセスに接続する方法は以下の通りです。

- RMI接続
- プロセスIDで接続（ローカル接続）
- 表示名で接続（ローカル接続）

引数指定なし、`-h,--host,-p,--port`指定時はRMI接続を行い、`-i,--pid`指定時はプロセスIDで接続、`-d,--dispName`は表示名で接続で接続を行う。
パラメータの優先順位は以下の通り。

1. プロセスID（-i,--pid）
2. 表示名（-d,--dispName）
3. それ以外（-h,--host,-p,--port,パラメータ指定なし）

以下より接続方法の解説を行う。

#### RMI接続

ホスト名、ポート番号を指定してRMI接続する方法。
この接続方法で接続するためには、以下のシステムプロパティをJava起動時に指定する必要がある。

```
-Dcom.sun.management.jmxremote.port=5000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

このアプリケーションでは認証とSSLは考慮していないため、上記3つのシステムプロパティを指定する必要がある。

指定できるパラメータは以下の通り。

- ホスト名（-h,--host）
  - 接続するホスト名を指定する。
  - デフォルト値は`localhost`となっている。
- ポート番号（-p,--port）
  - `com.sun.management.jmxremote.port`で指定したポート番号を指定する。
  - デフォルト値は`5000`となっている。

#### プロセスIDを指定して接続

プロセスIDを指定してローカル接続を行う方法。
指定できるパラメータは以下の通り。

- プロセスID（-i,--pid）
  - 起動したプロセスのIDを指定する。

#### JavaVMの表示名を指定して接続

表示名を指定してローカル接続する方法。
表示名とは、`com.sun.tools.attach.VirtualMachine`の`list`メソッドで取得したリストに格納されている`com.sun.tools.attach.VirtualMachineDescriptor`クラスの`displayName`メソッドで取得できる文字列。
出力例は以下の通り。

```
id　　　　　：12212
displayName ：examples.jmx.ProcessMonitor -d aaaaa
name　　　　：sun
type　　　　：windows
```

指定できるパラメータは以下の通り。

- 表示名（-d,--dispName）
  - 起動したプロセスのIDを指定する。<br>指定した値が表示名に含まれていたプロセスに接続する。<br>接続するプロセスは一番初めに一致したプロセスのみとなる。

## バージョン情報

| ライブラリ | バージョン |
| --- | --- |
| Java | 13 |
| Spring Boot | 2.2.5 |
| Apache CLI | 1.4 |
