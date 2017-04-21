package ru.dedoxyribose.yandexschooltest.util;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Ryan on 01.02.2017.
 */

/**
 * Перехватчик, используемый в тестах; задаёт правила, по которым подставляется фейковый ответ на запросы к серверу
 */

public class FakeInterceptor implements Interceptor {

    /**
     * Объект, представляющий ответ на запрос. Хранит код ответа и сообщение. Если код 0,
     * будет сгенерировано java.net.UnknownHostException, т.е. имитация отключённого интернета, например.
     */
    private class ResultObject {
        private int code=200;
        private String message="";
        public ResultObject(int code, String message) {
            this.code=code;
            this.message=message;
        }
    }


    private Map<String, ResultObject> mExactMatches = new HashMap<>();   //здесь хранятся ответы на запрос, полностью совпавший с правилом
    private Map<String, ResultObject> mPartialMatches = new HashMap<>();   //а здесь частичное совпадение (подстрока)

    @Override
    public Response intercept(Chain chain) throws IOException {


        // Get Request URI.
        final URI uri = chain.request().url().uri();
        // Get Query String.
        final String query = uri.getQuery();

        //System.out.println("entry uri path="+uri.getPath());

        for (Map.Entry<String, ResultObject> entry:mExactMatches.entrySet()) {
            if (entry.getKey().equals(uri.getPath())) {

                if (entry.getValue().code>0) {
                    return new Response.Builder()
                            .code(entry.getValue().code)
                            .message(entry.getValue().message)
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_0)
                            .body(ResponseBody.create(MediaType.parse("text/html"), entry.getValue().message.getBytes()))
                            .addHeader("content-type", "text/html")
                            .build();
                }
                else {
                    throw new java.net.UnknownHostException();
                }


            }
        }

        for (Map.Entry<String, ResultObject> entry:mPartialMatches.entrySet()) {
            if (uri.getPath().contains(entry.getKey())) {

                if (entry.getValue().code>0) {
                    return new Response.Builder()
                            .code(entry.getValue().code)
                            .message(entry.getValue().message)
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_0)
                            .body(ResponseBody.create(MediaType.parse("text/html"), entry.getValue().message.getBytes()))
                            .addHeader("content-type", "text/html")
                            .build();
                }
                else {
                    throw new java.net.UnknownHostException();
                }


            }
        }

        return null;
    }

    /**
     * Добавить частичное правило. Сработает, если url будет содержать подстроку, равную request.
     * Выдаст 200, если ответ не пустой или java.net.UnknownHostException в противном случае
     * @param request  подстрока поиска
     * @param response  заготовленный ответ
     */
    public void addContainRule(String request, String response) {
        if (response!=null)
            mPartialMatches.put(request, new ResultObject(200, response));
        else mPartialMatches.put(request, new ResultObject(0, ""));
    }

    /**
     * Добавить частичное правило. Сработает, если url будет содержать подстроку, равную request.
     * Выдаст code, если ответ не пустой или java.net.UnknownHostException в противном случае
     * @param request подстрока поиска
     * @param code код, который нужно вернуть
     * @param response заготовленный ответ
     */
    public void addContainRule(String request, int code, String response) {
        if (response!=null)
            mPartialMatches.put(request, new ResultObject(code, response));
        else mPartialMatches.put(request, new ResultObject(0, ""));
    }

    /**
     * Добавить точное правило. Сработает, если url будет равен request.
     * Выдаст 200, если ответ не пустой или java.net.UnknownHostException в противном случае
     * @param request  подстрока поиска
     * @param response  заготовленный ответ
     */
    public void addRule(String request, String response) {
        if (response!=null)
            mExactMatches.put(request, new ResultObject(200, response));
        else mExactMatches.put(request, new ResultObject(0, ""));
    }

    /**
     * Добавить частичное правило. Сработает, если url будет равен request.
     * Выдаст code, если ответ не пустой или java.net.UnknownHostException в противном случае
     * @param request подстрока поиска
     * @param code код, который нужно вернуть
     * @param response заготовленный ответ
     */
    public void addRule(String request, int code, String response) {
        if (response!=null)
            mExactMatches.put(request, new ResultObject(code, response));
        else mExactMatches.put(request, new ResultObject(0, ""));
    }

}
