package jp.gr.java_conf.mkh.wordbook.entity;

import android.content.ContentValues;

import java.io.Serializable;

/**
 * WORDBOOKテーブルの1レコードを表すエンティティクラス
 */
public class WordBookEntity implements Serializable {

    /** ID */
    private long id;
    /** 単語 */
    private String word;
    /** 分類 */
    private String category;
    /** 意味 */
    private String meaning;
    /** 練習対象。<br> 0:FALSE<br> 1:TRUE */
    private int trainingTarget;
    /** その他 */
    private String other;
    /** 作成日 */
    private String insertedDate;
    /** 更新日 */
    private String updatedDate;
    /** ふりがな */
    private String kana;

    /**
     * 更新日を返す。
     * @return 更新日
     */
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * 更新日を設定する。
     * @param updatedDate 更新日
     */
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * IDを返す。
     * @return ID
     */
    public long getId() {
        return id;
    }

    /**
     * IDを設定する。
     * @param id ID
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * 単語を返す。
     * @return 単語
     */
    public String getWord() {
        return word;
    }

    /**
     * 単語を設定する。
     * @param word 単語
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * 分類を返す。
     * @return 分類
     */
    public String getCategory() {
        return category;
    }

    /**
     * 分類を設定する。
     * @param category 分類
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 意味を返す。
     * @return 意味
     */
    public String getMeaning() {
        return meaning;
    }

    /**
     * 意味を設定する。
     * @param meaning 意味
     */
    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    /**
     * 練習対象を返す。<br> 0:FALSE<br> 1:TRUE
     * @return 練習対象
     */
    public int getTrainingTarget() {
        return trainingTarget;
    }

    /**
     * 練習対象を設定する。<br> 0:FALSE<br> 1:TRUE
     * @param trainingTarget 練習対象
     */
    public void setTrainingTarget(int trainingTarget) {
        this.trainingTarget = trainingTarget;
    }

    /**
     * その他を返す。
     * @return その他
     */
    public String getOther() {
        return other;
    }

    /**
     * その他を設定する。
     * @param other その他
     */
    public void setOther(String other) {
        this.other = other;
    }

    /**
     * 登録日を返す。
     * @return 登録日
     */
    public String getInsertedDate() {
        return insertedDate;
    }

    /**
     * 登録日を設定する。
     * @param insertedDate 登録日
     */
    public void setInsertedDate(String insertedDate) {
        this.insertedDate = insertedDate;
    }

    /**
     * ふりがなを返す。
     * @return ふりがな
     */
    public String getKana() {
        return kana;
    }

    /**
     * ふりがなを設定する。
     * @param kana ふりがな
     */
    public void setKana(String kana) {
        this.kana = kana;
    }

    /**
     * 保持している値をContentValuesに変換する。
     * @return 保持している値
     */
    public ContentValues getContentValues() {
        ContentValues value = new ContentValues();
        value.put("word", word);
        value.put("category", category);
        value.put("meaning", meaning);
        value.put("training_target", trainingTarget);
        value.put("other", other);
        value.put("inserted_date", insertedDate);
        value.put("updated_date", updatedDate);
        value.put("kana", kana);

        return  value;
    }
}
