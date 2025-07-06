package com.prog3.security.Interceptors;

import com.prog3.security.Services.ValidatorsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
    @Autowired
    private ValidatorsService validatorService;
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler)
                            throws Exception {
        boolean succes = this.validatorService.validationRolePermission(request, request.getRequestURI(), request.getMethod());

        System.out.println("Interceptor: " + request.getHeader("Authorization"));
        System.out.println("Interceptor: " + request.getRequestURI());
        System.out.println("Interceptor: " + request.getMethod());
        System.out.println("Interceptor: " + succes); // Imprime en consola el resultado de la validación

        return succes; // Lógica a ejecutar antes de que se maneje la solicitud por el controlador
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // Lógica a ejecutar después de que se haya manejado la solicitud por el controlador
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        // Lógica a ejecutar después de completar la solicitud, incluso después de la renderización de la vista
    }
}
