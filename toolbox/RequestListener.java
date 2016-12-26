package onelinelibrary.com.onelinelibrary.main_module.toolbox;

import onelinelibrary.com.onelinelibrary.main_module.Response.ErrorListener;
import onelinelibrary.com.onelinelibrary.main_module.Response.Listener;

public interface RequestListener< T > extends Listener< T >, ErrorListener {
}
