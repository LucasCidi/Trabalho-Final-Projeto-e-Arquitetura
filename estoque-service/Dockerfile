FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia o pom e o código-fonte
COPY pom.xml .
COPY src ./src

# Build do jar (sem testes)
RUN mvn -B package -DskipTests

# --- Imagem final ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o jar gerado
COPY --from=builder /app/target/*.jar app.jar

# Porta interna do serviço de estoque (ajusta se necessário)
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
