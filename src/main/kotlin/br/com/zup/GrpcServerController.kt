package br.com.zup

import io.micronaut.grpc.server.GrpcEmbeddedServer
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.inject.Inject

// Simulando a queda do servidor para verificar no HealthCheck
@Controller
class GrpcServerController(@Inject val grpcServer: GrpcEmbeddedServer) {

    @Get("/grpc-server/stop")
    fun stop(): HttpResponse<String> {

        grpcServer.stop()

        return HttpResponse.ok("Its running? ${grpcServer.isRunning}")
    }
}