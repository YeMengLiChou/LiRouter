# LiRouter 路由框架
个人学习 Ksp + Asm 的产物。

# 用法
## 路由声明
使用 `@Route` 注解在 Activity/Fragment 上：
```kotlin
@Router(url = "lirouter://first")
class FirstActivity: AppCompatActivity() {
    // ...
}
```

使用 `LiRouter` 进行跳转，携带参数：
- 允许通过 `withXxx` 方法携带参数
- 允许在 url 内使用 `baseUrl?key=value&key1=value1` 的形式携带参数
```kotlin
LiRouter
    .route(this, url)
    .withInt("int_arg", 1)
    .withShort("short_arg", 2)
    .withLong("long_arg", 3)
    .withFloat("float_arg", 4.0f)
    .withDouble("double_arg", 5.0)
    .withBoolean("boolean_arg", true)
    .withString("string_arg", "123")
    .withSerializable("pair_arg", Pair("12", 1))
    .withParcelable("parcelable_arg", Bundle().apply { putChar("1", '1') })
    .navigate(object : NavigationCallback {
        override fun onRouteLost(navigator: RouteNavigator) {
            Toast.makeText(this@MainActivity, "not found", Toast.LENGTH_SHORT).show()
        }
        override fun onRouteError(navigator: RouteNavigator, throwable: Throwable) {
        }
        override fun onRouteFound(navigator: RouteNavigator) {
        }
        override fun onRouteNavigate(navigator: RouteNavigator) {
            Toast.makeText(this@MainActivity, "navigated", Toast.LENGTH_SHORT).show()
        }
    })
```

## 自动注入
使用 `@AutoWired` 注解在 Activity/Fragment 内需要自动注入的字段上，然后在 `onCreate` 中调用 `LiRouter.autowire(this)` 即可：
```kotlin
@Router("lirouter://first")
class FirstActivity: AppCompatActivity() {
    @Autowired(names = ["string_arg"])
    var stringArg: String? = null

    @Autowired(names = ["int_arg"])
    var intArg: Int = 0

    @Autowired(names = ["long_arg"])
    var longArg: Long = 0L

    @Autowired(names = ["float_arg"])
    var floatArg: Float = 0f

    @Autowired(names = ["double_arg"])
    var doubleArg: Double = 0.0

    @Autowired(names = ["boolean_arg"])
    var booleanArg: Boolean = false

    @Autowired(names = ["pair_arg"])
    var pairArg: Pair<String, *>? = null

    @Autowired(names = ["parcelable_arg"])
    var parcelableArg: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LiRouter.autowire(this)
    }
}
```

## 路由拦截
使用 `@RouteInterceptor` 注解在拦截器类上，拦截器需要实现 `IRouteInterceptor` 接口
- 在 `match` 方法中返回是否匹配当前路由
- 在 `onIntercept` 方法中返回新的路由：

```kotlin
@RouteInterceptor(priority = 1)
class LoginInterceptor: IRouteInterceptor {
    override fun match(navigator: RouteNavigator): Boolean {
        return navigator.url.baseUrl == "lirouter://first"
    }

    override fun onIntercept(navigator: RouteNavigator): RouteNavigator {
        return LiRouter.route(navigator.context, "lirouter://login").build()
    }
}
```


## ServiceProvider 服务
模块之间通过 `ServiceProvider` 来获取接口相关实现；

提供服务的接口需要继承 `IServiceProvider` 接口，内部是服务提供的功能的相关方法；

对应实现需要实现该接口，并且使用 `@Service` 注解标识：
- `singleton`：是否为单例，每次获取都会返回同一个实例
- `returnType`：当注解使用在方法上时而返方法返回值不是接口类型时，需要指定接口类型

> 目前仅支持 **一对一** 的实现。

例子：
服务接口声明
```kotlin
interface ILoginService: IServiceProvider {
    fun login()
}
```

接口实现：
```kotlin
@Service(singleton = true)
class LoginServiceImpl: ILoginService {
    override fun login() {
        //...
    }
}

// 也可以通过静态方法或top-level方法提供实现
// 当返回值不是接口类型时，需要指定接口类型 returnType
@Service(singleton = true, returnType = ILoginService::class)
fun loginServiceProvider() = LoginServiceImpl() 

// 返回值是接口类型时，不需要指定接口类型
@Service(singleton = true)
fun loginServiceProvider(): ILoginService = LoginServiceImpl() 
```

获取实现：
```kotlin
val loginService = ServiceProvider.getService(ILoginService::class.java)
loginService?.login()

// 或者使用提供的扩展方法
ILoginService::class.impl()?.login()
```