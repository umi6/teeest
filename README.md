
### JDK17のインストール
現在のJDKの最新バージョンは25ですが、gradleやProcessing4との相性の関係で**このテンプレートではJDK17を使用します**。
[こちらのリンク](https://adoptium.net/temurin/releases/?version=17)からインストールしておいてください。
またインストール時に、**「カスタムセットアップ」画面の下部にある「場所」の項目を確認しておいてください**。
## テンプレートの利用方法
2通りの方法があります
### A. Template Repository機能の利用
1. トップページ右上の**Use this template**ボタンを押します。
2. リポジトリ名、概要を入力し、リポジトリを作成します。
#### Visual Studio Code の場合
3. VSCodeを開き、左タブの最も上にある**エクスプローラー**を選択します。（もしくはCtrl+Shift+E）
4. **リポジトリの複製**ボタンを押します。
5. 上部の入力欄で「GitHubから複製」を選択 -> 先ほど作成したGitHubのリポジトリを選択します。
6. ローカルリポジトリとして利用するフォルダを選択します。
7. 数分待つと.gradleフォルダなどのいくつかのフォルダが自動で生成されます。
8. 次のステップ(**gradle.propertiesの編集**)へ
### B. 圧縮ファイルのダウンロード
1. [releases](https://github.com/lpmmoyojs/ProcessingToolTemplate/releases)より、テンプレートの圧縮ファイルをダウンロードします。
#### Visual Studio Code の場合
2. ダウンロードしたファイルを解凍し、好きなPC上の場所に移動させておきます。
3. VSCodeを開き、「ファイル」->「フォルダを開く」を選択し、解凍したテンプレートを選択します。
4. 左タブの**ソース管理**を選択（もしくはCtrl+Shift+G）し、最初のコミットを行います。
5. 次のステップ(**gradle.propertiesの編集**)へ
## gradle.propertiesの編集
#### processingPath
Processingのツールを作るのに必要な機能を利用するため、**"app-4.\*.jar"**と、**"core-4.\*.jar"**を含むフォルダを指定する必要があります。
通常ではこれらのファイルは`C:/Program Files/Processing/app`内にあると思いますので、確認してください。
**（Processing バージョン4.4.10ではこのようなフォルダ構造ですが、Processingのアップデートに伴ってフォルダ構造やjarファイル名が変わる可能性があります）**
#### processingUserPath
作成したツールのフォルダを作成するため、Processingのスケッチフォルダを指定します。
Processingを起動し、「ファイル」->「設定...」を開くと、一番上に「スケッチブックの場所」という項目があります。そこに書かれているフォルダパスを指定してください。
#### org.gradle.java.home
インストールしたJDK17のフォルダパスを指定します。
Adoptiumからインストールする際、インストール先を変更していなければ`C:/Program Files/Eclipse Adoptium`フォルダ内にあると思います。
## tool.propertiesの編集
ツールの情報を入力します。
**注**
- `minRevision`：変える必要はありません。
- （`maxRevision`：Processingのバージョンの上限を指定出来ます。)
- `version`：必ず**整数値**で指定してください。
## 実行方法
1. **ビルド**：ターミナルなどを開き、`gradle clean build`を実行します。
2. **ファイルのコピー**：ターミナルなどを開き、`gradle installTool`を実行します。(ビルドしたファイルをProcessingのtoolsフォルダ内にコピーする操作です。)
3. **確認**：Processingを起動（もしくは再起動）し、ツールを確認してください。

