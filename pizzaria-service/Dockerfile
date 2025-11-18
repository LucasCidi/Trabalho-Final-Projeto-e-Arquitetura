FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia o pom e o código-fonte
COPY pom.xml .
COPY src ./src

# Build do jar (sem rodar testes)
RUN mvn -B package -DskipTests

# --- Imagem final, mais enxuta ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o jar gerado do estágio builder
# Usa wildcard pra não depender do nome exato do jar
COPY --from=builder /app/target/*.jar app.jar

# Porta interna do serviço da pizzaria (ajusta se teu app tiver outra)
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
