package xyz.cleangone.e2.web.manager;

import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;

import java.util.UUID;

/**
 * copied from org.vaadin.viritin.util
 */
public class BrowserCookie
{

    public BrowserCookie() { }

    public static void setCookie(String key, String value) {
        setCookie(key, value, "/");
    }

    public static void setCookie(String key, String value, String path) {
        JavaScript.getCurrent().execute(String.format("document.cookie = \"%s=%s; path=%s\";", new Object[]{key, value, path}));
    }

    public static void detectCookieValue(String key, final BrowserCookie.Callback callback) {
        final String callbackid = "viritincookiecb" + UUID.randomUUID().toString().substring(0, 8);
        JavaScript.getCurrent().addFunction(callbackid, new JavaScriptFunction() {
            private static final long serialVersionUID = -3426072590182105863L;

            public void call(JsonArray arguments) {
                JavaScript.getCurrent().removeFunction(callbackid);
                if(arguments.length() == 0) {
                    callback.onValueDetected((String)null);
                } else {
                    callback.onValueDetected(arguments.getString(0));
                }

            }
        });
        JavaScript.getCurrent().execute(String.format("var nameEQ = \"%2$s=\";var ca = document.cookie.split(';');for(var i=0;i < ca.length;i++) {var c = ca[i];while (c.charAt(0)==' ') c = c.substring(1,c.length); if (c.indexOf(nameEQ) == 0) {%1$s( c.substring(nameEQ.length,c.length)); return;};} %1$s();", new Object[]{callbackid, key}));
    }

    public interface Callback {
        void onValueDetected(String var1);
    }
}
