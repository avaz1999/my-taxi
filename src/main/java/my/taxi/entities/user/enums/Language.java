package my.taxi.entities.user.enums;

/**
 * Created by Avaz Absamatov
 * Date: 9/12/2025
 */
public enum Language {
    UZ_LATN("uz-Latn"), UZ_CYRL("uz-Cyrl"), RU("ru"), EN("en");
    public final String bcp47;

    Language(String bcp47) {
        this.bcp47 = bcp47;
    }

}
