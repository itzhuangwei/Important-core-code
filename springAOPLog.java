@Aspect 
@Component 
public class test {


private static Logger logger = Logger.getLogger(test.class);
private static SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


@Autowired
private OperatorLogService<OperatorLog> operatorLogService; //自己创建，用来保存日志信息




@Pointcut("execution(* com.env.web.controller..*.*(..))") //切点
public void webRequestLog(){}


@Pointcut("execution(* com.env.web.controller..*.*(..))") //切点
public void webExceptionLog(){}


@Before("webRequestLog()") 
public void doBefore(JoinPoint joinPoint){
    try {   
    OperatorLog operatorLog = new OperatorLog();
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request= attributes.getRequest();
    String method = request.getMethod();
    //判断是否是post方法，如果是，则记录到日志表中
   // if("POST".equals(method)){
        long beginTime = System.currentTimeMillis();  
        String beanName = joinPoint.getSignature().getDeclaringTypeName(); //方法所在的类名
        String methodName = joinPoint.getSignature().getName()+"-"+method;//方法名
        String param = JSONUtil.obj2StringPretty(request.getParameterMap());//请求参数
        System.out.println(param);
        String uri = request.getRequestURI(); //接口名
        String url = request.getRequestURL().toString(); //url
        String remoteAddr = getIpAddr(request); //ip地址
        String sessionId = request.getSession().getId();
        Integer  uid = (Integer) request.getSession().getAttribute("uid"); //用户id
        if(uid != null){
        }
        operatorLog.setMethod(methodName);
        operatorLog.setBeanName(beanName);
        operatorLog.setIntf(uri);
        operatorLog.setUrl(url);
        Date date = new Date(beginTime);
        operatorLog.setRequestTime(date);
        operatorLog.setRequestIp(remoteAddr);
        operatorLog.setRequestParam(param);
        operatorLogService.save(operatorLog);
        //tlocal.set(operatorLog);
   // }
    } catch (Exception e) {
       e.printStackTrace();
    }
}






/** 
 * 异常通知 用于拦截异常日志 
 * 
 * @param joinPoint 
 * @param e 
 */  
@AfterThrowing(pointcut = "webExceptionLog()", throwing = "e")  
 public  void doAfterThrowing(JoinPoint joinPoint, Throwable e) {  
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();  
    HttpSession session = request.getSession();  
    //获取请求ip  
    String ip = request.getRemoteAddr();  
    //获取用户请求方法的参数并序列化为JSON格式字符串   
   try { 
        String method = request.getMethod();
        String param = JSONUtil.obj2StringPretty(request.getParameterMap());
        System.out.println(param);
        String beanName = joinPoint.getSignature().getDeclaringTypeName(); //方法所在的类名
        String methodName = joinPoint.getSignature().getName()+"-"+method;//方法名
        String uri = request.getRequestURI(); //接口名
        String url = request.getRequestURL().toString(); //url
        OperatorLog operatorLog = new OperatorLog();
        operatorLog.setExceptionName(e.getClass().getName());
        operatorLog.setExceptionMsg(e.getMessage());
        operatorLog.setMethod(methodName);
        operatorLog.setUrl(url);
        operatorLog.setIntf(uri);
        operatorLog.setRequestParam(param);
        operatorLog.setBeanName(beanName);
        long beginTime = System.currentTimeMillis();  
        Date date = new Date(beginTime);
        operatorLog.setRequestTime(date);
        operatorLog.setRequestIp(ip);
       
        //保存数据库  
        operatorLogService.save(operatorLog); 
        System.out.println("=====异常通知结束=====");  
   }  catch (Exception ex) {  
        //记录本地异常日志  
        e.printStackTrace();
        logger.error("==异常通知异常==");  
    }  
}  




/*@AfterReturning(returning="result",pointcut = "webRequestLog()")
public void doAfterReturning(Object result){
}
*/


private String getIpAddr(HttpServletRequest request){     
    String ip = request.getHeader("x-forwarded-for");
    if(ip==null || ip.length()==0 || "unknowm".equalsIgnoreCase(ip)){
        ip = request.getHeader("Proxy-Client-IP");
    }
    if(ip==null || ip.length()==0 || "unknowm".equalsIgnoreCase(ip)){
        ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if(ip==null || ip.length()==0 || "unknowm".equalsIgnoreCase(ip)){
        ip = request.getRemoteAddr();
    }
    return ip;
}
}
