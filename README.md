# Upload e Enriquecimento de Imagem API

API Spring Boot para upload de imagens com enriquecimento automático via IA (Groq) e processamento assíncrono via filas AMQP.

## 🚀 Funcionalidades

- **Upload de Imagens**: Suporte a arquivos multipart com validação de tipo
- **Processamento com IA**: Integração com Groq API para melhorar descrições
- **Processamento Assíncrono**: Fila AMQP para processamento em background
- **Armazenamento Cloud**: Upload automático para Cloudinary
- **Banco de Dados**: Persistência em PostgreSQL
- **API REST**: Endpoints para upload e consulta de resultados
- **Documentação**: Swagger/OpenAPI integrado

## 🛠️ Tecnologias

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **Spring AMQP (RabbitMQ)**
- **PostgreSQL**
- **Cloudinary**
- **Groq API**
- **Maven**
- **Lombok**

## 📋 Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- PostgreSQL
- RabbitMQ (CloudAMQP)
- Conta no Cloudinary
- Chave de API do Groq

## ⚙️ Configuração

### 1. Banco de Dados PostgreSQL

Configure as credenciais do banco no `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://seu-host:porta/nome-do-banco?sslmode=require
spring.datasource.username=seu-usuario
spring.datasource.password=sua-senha
spring.datasource.driverClassName=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### 2. Cloudinary

Configure suas credenciais do Cloudinary:

```properties
cloudinary.url=cloudinary://api-key:api-secret@cloud-name
cloudinary.secret=sua-api-secret
```

### 3. Groq API

Configure sua chave de API do Groq:

```properties
groq.api.key=sua-chave-groq
groq.api.url=https://api.groq.com/openai/v1/chat/completions
```

### 4. RabbitMQ (CloudAMQP)

Configure a conexão com RabbitMQ:

```properties
spring.rabbitmq.host=seu-host-cloudamqp.com
spring.rabbitmq.port=5671
spring.rabbitmq.username=seu-usuario
spring.rabbitmq.password=sua-senha
spring.rabbitmq.virtual-host=seu-vhost
spring.rabbitmq.ssl.enabled=true
```

### 5. Configurações da Fila AMQP

```properties
amqp.queue.name=image-processing-queue
amqp.exchange.name=image-processing-exchange
amqp.routing.key=image-processing
```

## 🚀 Execução

### 1. Clone o repositório

```bash
git clone <url-do-repositorio>
cd img
```

### 2. Configure as variáveis

Edite o arquivo `src/main/resources/application.properties` com suas credenciais.

### 3. Execute a aplicação

```bash
# Compilar e executar
mvn spring-boot:run

# Ou compilar separadamente
mvn clean compile
mvn spring-boot:run
```

### 4. Acesse a aplicação

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 📚 Endpoints da API

### Upload de Imagem

```http
POST /api/files/upload
Content-Type: multipart/form-data

arquivo: [arquivo de imagem]
nomeArquivo: nome-do-arquivo.jpg
descricao: Descrição da imagem
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "nomeArquivo": "nome-do-arquivo.jpg",
  "linkPublico": "https://res.cloudinary.com/...",
  "descricao": "Descrição da imagem",
  "conteudo": null
}
```

### Consulta de Resultado

```http
GET /api/files/{id}
```

**Resposta:**
```json
{
  "id": 1,
  "nomeArquivo": "nome-do-arquivo.jpg",
  "linkPublico": "https://res.cloudinary.com/...",
  "descricao": "Descrição original",
  "conteudo": "Descrição enriquecida pela IA"
}
```

## 🔄 Fluxo de Processamento

1. **Upload**: Usuário envia imagem via API
2. **Validação**: Sistema valida se é uma imagem válida
3. **Cloudinary**: Imagem é enviada para Cloudinary
4. **Fila AMQP**: Mensagem é enviada para fila de processamento
5. **Processamento IA**: Groq API melhora a descrição
6. **Persistência**: Resultado é salvo no banco de dados
7. **Consulta**: Usuário pode consultar o resultado pelo ID

## 🧪 Testes

### Executar todos os testes

```bash
mvn test
```

### Executar testes específicos

```bash
# Testes de serviço
mvn test -Dtest=GroqServiceTest
mvn test -Dtest=AmqpServiceTest

# Testes de controller
mvn test -Dtest=FileUploadControllerTest

# Testes de repositório
mvn test -Dtest=DadosImagemRepositoryTest
```

### Cobertura de testes

Os testes cobrem:
- ✅ Upload e validação de arquivos
- ✅ Integração com Cloudinary
- ✅ Processamento via fila AMQP
- ✅ Integração com Groq API
- ✅ Persistência no banco de dados
- ✅ Tratamento de erros

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/enriquecimento/upload/
│   │   ├── config/
│   │   │   └── AmqpConfig.java          # Configuração AMQP
│   │   ├── controller/
│   │   │   └── FileUploadController.java # Endpoints da API
│   │   ├── entity/
│   │   │   └── DadosImagem.java         # Entidade JPA
│   │   ├── repository/
│   │   │   └── DadosImagemRepository.java # Repositório JPA
│   │   ├── service/
│   │   │   ├── FileUploadService.java    # Serviço principal
│   │   │   ├── AmqpService.java          # Serviço AMQP
│   │   │   └── GroqService.java          # Serviço Groq API
│   │   └── ApiApplication.java           # Classe principal
│   └── resources/
│       └── application.properties        # Configurações
└── test/
    └── java/com/enriquecimento/upload/
        ├── controller/
        ├── service/
        └── repository/
```

## 🔧 Configurações Avançadas

### Variáveis de Ambiente

Você pode sobrescrever as configurações usando variáveis de ambiente:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydb
export GROQ_API_KEY=sua-chave
export CLOUDINARY_URL=sua-url-cloudinary
```

### Perfis Spring

Crie diferentes perfis para diferentes ambientes:

```bash
# Desenvolvimento
mvn spring-boot:run -Dspring.profiles.active=dev

# Produção
mvn spring-boot:run -Dspring.profiles.active=prod
```

## 🚨 Troubleshooting

### Erro de conexão com PostgreSQL

- Verifique se o PostgreSQL está rodando
- Confirme as credenciais no `application.properties`
- Teste a conexão manualmente

### Erro de conexão com RabbitMQ

- Verifique se o CloudAMQP está ativo
- Confirme as credenciais SSL
- Teste a conexão via console do CloudAMQP

### Erro de upload para Cloudinary

- Verifique a URL do Cloudinary
- Confirme as permissões da conta
- Verifique o limite de upload

### Erro de API do Groq

- Verifique a chave da API
- Confirme o limite de requisições
- Teste a API manualmente

## 📝 Logs

A aplicação gera logs detalhados para:

- Upload de arquivos
- Processamento de filas AMQP
- Chamadas para Groq API
- Operações de banco de dados
- Erros e exceções

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 📞 Suporte

Para dúvidas ou problemas:

- Abra uma issue no repositório
- Consulte a documentação do Swagger
- Verifique os logs da aplicação

---

**Desenvolvido com ❤️ usando Spring Boot e IA**
