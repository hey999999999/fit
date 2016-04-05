package jp.co.jiec.deltaspike;

import java.io.Closeable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanProvider;

/**
 * Functionalインターフェースを実装したクラスをCDI管理下にして実行する
 * @author K.Taira
 */
public class DeltaSpikeInvoker implements Closeable{
	private static final CdiContainer container;

	static{
		container = CdiContainerLoader.getCdiContainer();
	}

	public <T> T instantiate(Class<T> clazz){
		container.boot();
        ContextControl contextControl = container.getContextControl();
        contextControl.startContexts();
		try {
			return BeanProvider.injectFields(clazz.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void close(){
		if(container != null){
			container.shutdown();
		}
	}

	public static <T extends Runnable> void run(Class<T> clazz){
		invoke(clazz, r -> {r.run(); return null;});
	}

	public static <U, T extends Consumer<U>> void consume(Class<T> clazz, U u){
		invoke(clazz, t -> {t.accept(u); return null;});
	}

	public static <R, T extends Supplier<R>> R supply(Class<T> clazz){
		return invoke(clazz, t -> t.get());
	}

	public static <U, R, T extends Function<U, R>> R operate(Class<T> clazz, U u){
		return invoke(clazz, t -> t.apply(u));
	}

	public static <U, V, R, T extends BiFunction<U, V, R>> R operate(Class<T> clazz, U u, V v){
		return invoke(clazz, t -> t.apply(u, v));
	}

	public static <T,R> R invoke(Class<T> clazz, Function<T, R> func){
		try(DeltaSpikeInvoker invoker = new DeltaSpikeInvoker()){
			return func.apply(invoker.instantiate(clazz));
		}
	}

	public static String main(String... args){
		StackTraceElement[] elems = Thread.currentThread().getStackTrace();
		String methodName = elems[elems.length - 1].getMethodName();
		if(!methodName.equals("main")){
			throw new IllegalArgumentException("mainメソッドから実行されてない！");
		}
		String className = elems[elems.length - 1].getClassName();
		try {
			Class<?> c = Class.forName(className);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Class clazz = (Class<Function<String[], String>>)c;
			@SuppressWarnings("unchecked")
			String result = (String)DeltaSpikeInvoker.operate(clazz, args);
			return result;
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
}