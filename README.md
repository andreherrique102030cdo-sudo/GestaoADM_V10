# Gestão ADM — Android v8

Esta versão continua o projeto a partir da v6 e implementa a primeira camada funcional da interface do aplicativo em Jetpack Compose.

## Implementado nesta versão
- Cadastro real de pessoas no Firestore.
- Lista de pessoas carregada do Firestore com busca.
- Formulário de novo cadastro com nome, telefone, e-mail, nascimento, estado civil e função.
- Regras de acesso continuam no Firestore.
- Login real preparado com Firebase Authentication.
- Perfis: ADMINISTRADOR, PASTOR, SECRETARIA, LIDER e MEMBRO.
- Dashboard inicial com resumo da igreja.
- Navegação inferior: Início, Pessoas, Agenda, Finanças e Mais.
- Tela de pessoas com busca visual e lista.
- Agenda de eventos.
- Resumo financeiro.
- Área Mais com relatórios, configurações, usuários/permissões e segurança.
- Logout e sessão persistente via Firebase.
- Identidade visual inspirada na referência enviada, sem usar "Campo Monte Castelo" no nome do aplicativo.

## Próxima integração
As listas e indicadores desta versão são dados de interface. O próximo passo é ligar Pessoas, Agenda, Finanças, Relatórios e Configurações ao Firestore, mantendo as regras de menor privilégio da v6.

## Firebase
Para executar o login real, criar o projeto Firebase da igreja, ativar Authentication > Email/Password, criar Firestore e colocar `google-services.json` em `app/`.
