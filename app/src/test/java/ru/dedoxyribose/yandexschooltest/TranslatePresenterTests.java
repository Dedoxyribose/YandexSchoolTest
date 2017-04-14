package ru.dedoxyribose.yandexschooltest;

import android.content.Context;
import android.test.mock.MockContext;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Example;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.SupportedLangs;
import ru.dedoxyribose.yandexschooltest.model.entity.Translation;
import ru.dedoxyribose.yandexschooltest.model.entity.Word;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslatePresenter;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslateView;
import ru.dedoxyribose.yandexschooltest.util.AppSession;
import ru.dedoxyribose.yandexschooltest.util.FakeInterceptor;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.ServerApi;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static ru.dedoxyribose.yandexschooltest.util.RetrofitHelper.API_URL;

/**
 * Created by Ryan on 05.04.2017.
 */

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
public class TranslatePresenterTests {

    TranslatePresenter translatePresenter;
    TranslateView translateView;
    MockContext mockContext;
    ServerApi serverApi;
    FakeInterceptor fakeInterceptor;
    Gson gson;
    AppSession appSession;

    @Before
    @PrepareForTest({Log.class, EventBus.class, Context.class})
    public void before() {


        PowerMockito.mockStatic(Context.class);
        mockContext= Mockito.mock(MockContext.class);

        YandexSchoolTestApplication.buildComponent(true, mockContext);

        Mockito.when(mockContext.getString(R.string.Error)).thenReturn("error");
        Mockito.when(mockContext.getString(R.string.ConnectionError)).thenReturn("connError");
        Mockito.when(mockContext.getString(R.string.CheckConnection)).thenReturn("checkConn");
        Mockito.when(mockContext.getString(R.string.BadKey)).thenReturn("badKey");
        Mockito.when(mockContext.getString(R.string.DirectionNotSupported)).thenReturn("dirNotSupported");
        Mockito.when(mockContext.getString(R.string.dict_key)).thenReturn("");
        Mockito.when(mockContext.getString(R.string.trans_key)).thenReturn("");

        /*when(mockContext.getString(R.string.PassTooShort)).thenReturn("PassTooShort");
        when(mockContext.getString(R.string.NoConn)).thenReturn("NoConn");*/


        /*gson = new GsonBuilder()
                .registerTypeAdapter(Word.class, new Word.WordConverter())
                .registerTypeAdapter(Def.class, new Def.DefConverter())
                .registerTypeAdapter(Example.class, new Example.ExampleConverter())
                .registerTypeAdapter(Record.class, new Record.RecordConverter())
                .registerTypeAdapter(Translation.class, new Translation.TranslationConverter())
                .registerTypeAdapter(SupportedLangs.class, new SupportedLangs.SupportedLangsConverter())
                .create();*/



        PowerMockito.mockStatic(RetrofitHelper.class);
        PowerMockito.mockStatic(AppSession.class);

        appSession = YandexSchoolTestApplication.getAppSessionComponent().getAppSession();
        RetrofitHelper retrofitHelper=YandexSchoolTestApplication.getAppSessionComponent().getRetrofitHelper();
        serverApi = retrofitHelper.getServerApi();
        fakeInterceptor = retrofitHelper.getFakeInterceptor();


        ArrayList<Lang> langs = new ArrayList<>();

        langs.add(new Lang("ru", "Русский", 1));
        langs.add(new Lang("en", "Английский", 1));
        langs.add(new Lang("fr", "Французский", 1));

        Mockito.when(appSession.getLangs()).thenReturn(langs);
        Mockito.when(appSession.isSyncTranslation()).thenReturn(false);
        Mockito.when(appSession.getLastLangFrom()).thenReturn("ru");
        Mockito.when(appSession.getLastLangTo()).thenReturn("en");
        Mockito.when(appSession.getLastText()).thenReturn("");
        Mockito.when(appSession.isShowDict()).thenReturn(true);
        Mockito.when(appSession.getContext()).thenReturn(mockContext);

        PowerMockito.mockStatic(Log.class);
        BDDMockito.given(Log.d(anyString(), anyString())).willReturn(1);

        PowerMockito.mockStatic(EventBus.class);
        EventBus eventBus=mock(EventBus.class);
        BDDMockito.given(EventBus.getDefault()).willReturn(eventBus);


        Mockito.when(appSession.isReturnTranslate()).thenReturn(true);


    }

    @Test
    @PrepareForTest({RetrofitHelper.class, AppSession.class, Log.class, EventBus.class, Context.class})
    public void sentRequestWithNoConnection_gotConnectionError() {

        translatePresenter=new TranslatePresenter();
        translateView = mock(TranslateView.class);
        translatePresenter.attachView(translateView);

        fakeInterceptor.addContainRule("lookup", null);
        fakeInterceptor.addContainRule("translate", null);

        translatePresenter.textChanged("яблоко");
        translatePresenter.returnPressed();

        verify(translateView, atLeastOnce()).showError(false, null, null, false);
        verify(translateView, atLeastOnce()).showError(true, "connError", "checkConn", true);

    }

    @Test
    @PrepareForTest({RetrofitHelper.class, AppSession.class, Log.class, EventBus.class, Context.class})
    public void getResponseBadLangs_gotError() {

        translatePresenter=new TranslatePresenter();
        translateView = mock(TranslateView.class);
        translatePresenter.attachView(translateView);

        String answer="{\"code\":501,\"message\":\"The specified language is not supported\"}";

        fakeInterceptor.addContainRule("lookup", 400, answer);
        fakeInterceptor.addContainRule("translate", 400, answer);

        translatePresenter.textChanged("яблоко");
        translatePresenter.returnPressed();

        System.out.print("our mock is "+translateView);
        verify(translateView, atLeastOnce()).showError(false, null, null, false);
        verify(translateView, atLeastOnce()).showError(true, "error", "dirNotSupported", false);

    }

    @Test
    @PrepareForTest({RetrofitHelper.class, AppSession.class, Log.class, EventBus.class, Context.class})
    public void getResponseBadKey_gotError() {

        translatePresenter=new TranslatePresenter();
        translateView = mock(TranslateView.class);
        translatePresenter.attachView(translateView);


        String answer="{\"code\":401,\"message\":\"API key is invalid\"}";

        fakeInterceptor.addContainRule("lookup", 400, answer);
        fakeInterceptor.addContainRule("translate", 400, answer);

        translatePresenter.textChanged("яблоко");
        translatePresenter.returnPressed();

        verify(translateView, atLeastOnce()).showError(false, null, null, false);
        verify(translateView, atLeastOnce()).showError(true, "error", "badKey", false);

    }

}
