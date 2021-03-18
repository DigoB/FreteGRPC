package br.com.zup

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer : CalculaFreteServiceGrpc.CalculaFreteServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)
    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        logger.info("Calulando frete para request: $request")

        val cep = request?.cep
        if (cep == null || cep.isBlank()) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("O cep deve ser informado")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("Cep em formato inválido")
                .augmentDescription("Formato esperado: 00000-000")
                .asRuntimeException()

            responseObserver?.onError(e)
        }

        // Simulando erro de verificacao de segurança
        if (cep.endsWith("333")) {

            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("Usuário não pode acessar esse recurso!")
                .addDetails(Any.pack(ErrorDetails.newBuilder()
                    .setCode(401)
                    .setMessage("Token expirado").build()))
                .build()

            val e = StatusProto.toStatusRuntimeException(statusProto)

            responseObserver?.onError(e)
        }

        var valor = 0.0

        // Tratamento para exceção não esperada
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.0) // Deveria ser uma logia complexa
            if (valor > 100.0) {
                throw IllegalStateException("Erro inexperado ao executar lógica de negócio!")
            }
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e) // É anexao ao status de erro mas não é enviado ao Client
                .asRuntimeException())
        }

        val response = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(valor)
            .build()

        logger.info("Frete calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()

    }
}