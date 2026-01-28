# BlinkLink 🔗

**API de Encurtamento de URLs: Simples, Eficiente e Containerizada.**

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/API-Swagger-green?logo=swagger)](https://swagger.io/)

---

## 📖 Sobre o Projeto

BlinkLink é uma **API REST** desenvolvida para resolver o problema de URLs longas com foco em **performance** e **integridade de dados**. A aplicação utiliza uma estratégia matemática de geração de IDs únicos (Base62) para evitar colisões e garantir escalabilidade.

**Projeto open-source focado em boas práticas de desenvolvimento backend.**

---

## 🛠 Tech Stack (v1.0.0)

- **Java 21 (LTS)** & **Spring Boot 4.0**
- **PostgreSQL 17** (Persistência Relacional)
- **Flyway** (Migração de Dados)
- **Docker & Docker Compose** (Ambiente Reprodutível)
- **SpringDoc OpenAPI** (Swagger UI)

---

## 🧠 Arquitetura & Decisões de Design

### Estratégia de ID: Two-Step Save

O BlinkLink utiliza uma estratégia de **persistência em duas etapas** para gerar códigos curtos únicos:

1. **Primeira Gravação:** A URL original é salva no banco de dados, gerando um ID sequencial único.
2. **Conversão Base62:** O ID numérico é convertido para Base62 (0-9, a-z, A-Z), criando um código curto legível e URL-safe.
3. **Segunda Gravação:** O código curto é atualizado na mesma transação.

**Por que Base62?** Garantimos **unicidade matemática** e **performance** ao evitar colisões de hash. Cada ID sequencial resulta em um código único e compacto.

### Segregação de Responsabilidades

A API separa claramente dois fluxos distintos:

- **API de Criação:** `POST /url/v1/shorten` - Endpoint versionado para criação de links encurtados.
- **Rota de Acesso:** `GET /{shortCode}` - Rota na raiz para redirecionamento rápido (HTTP 302).

Essa separação permite URLs mais curtas para usuários finais e clareza na arquitetura da API.

---

## 📊 Fluxo

```mermaid
sequenceDiagram
    participant Client
    participant API as API Controller
    participant Service as URL Service
    participant DB as PostgreSQL
    participant Encoder as Base62 Encoder

    Client->>API: POST /url/v1/shorten {url}
    API->>Service: shorten(url)
    Service->>DB: save(UrlEntity) - 1ª gravação
    DB-->>Service: ID sequencial gerado
    Service->>Encoder: encode(ID)
    Encoder-->>Service: shortCode Base62
    Service->>DB: update shortCode - 2ª gravação
    DB-->>Service: UrlEntity completa
    Service-->>API: UrlEntity
    API-->>Client: {shortUrl}
    
    Note over Client,Encoder: Fluxo de Redirecionamento
    
    Client->>API: GET /{shortCode}
    API->>Service: resolve(shortCode)
    Service->>DB: findByShortCode(shortCode)
    DB-->>Service: UrlEntity
    Service-->>API: UrlEntity
    API-->>Client: HTTP 302 Redirect
```

---

## 🚀 Como Rodar (Quick Start)

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/PabloTzeliks/blink-link.git
   cd blink-link
   ```

2. **Suba os containers:**
   ```bash
   docker-compose up -d
   ```

3. **Acesse a documentação Swagger:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

---

## 🔮 Roadmap v2.0.0 (Próximos Passos)

**Foco na robustez técnica e preparação para escala.**

- [ ] **Refatoração Arquitetural:** Evolução dos DTOs existentes (por exemplo, `UrlRequest` e `UrlResponse`) e maior desacoplamento entre API e Banco de Dados.
- [ ] **Tratamento de Erros Profissional:** GlobalExceptionHandler seguindo a RFC 7807 (Problem Details).
- [ ] **Testes Automatizados:** Implementação de testes unitários (JUnit + Mockito) e de integração para garantir a estabilidade.
- [ ] **Feature: Expiração de Links (TTL):** Implementação de jobs agendados para desativar/limpar links antigos automaticamente.
- [ ] **CI/CD Básico:** Pipeline no GitHub Actions para build e testes automáticos a cada Push.

*Nota:* O foco será na engenharia de software, mantendo a infraestrutura on-premise (sem Cloud/Cache por enquanto).

---

## 📞 Contato & Autor

<div align="center">

**Pablo Ruan Tzeliks**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/pablo-ruan-tzeliks/)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/PabloTzeliks)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:arq.pabloo@gmail.com)

*Desenvolvedor Backend | Java & Spring Boot Enthusiast*

</div>

---

<div align="center">

**⭐ Se este projeto foi útil, considere dar uma estrela!**

*Construído com ☕ e dedicação*

</div>
