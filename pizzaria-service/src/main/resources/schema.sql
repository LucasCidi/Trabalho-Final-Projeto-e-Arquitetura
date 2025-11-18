create table if not exists clientes(
  cpf varchar(15) not null primary key,
  nome varchar(100) not null,
  password varchar(100) not null,
  enabled boolean not null,
  celular varchar(20) not null,
  endereco varchar(255) not null,
  email varchar(255) not null
);

create table if not exists authorities (
  cpf varchar(50) not null,
  authority varchar(50) not null, 
  constraint fk_authorities_clientes foreign key(cpf) references clientes(cpf)
);

create unique index if not exists ix_auth_cpf on authorities (cpf, authority);

create table if not exists ingredientes (
  id bigint primary key,
  descricao varchar(255) not null
);

create table if not exists itensEstoque(
    id bigint primary key,
    quantidade int,
    ingrediente_id bigint,
    foreign key (ingrediente_id) references ingredientes(id)
);

-- Tabela Receita
create table if not exists receitas (
  id bigint primary key,
  titulo varchar(255) not null
);

-- Tabela de relacionamento entre Receita e Ingrediente
create table if not exists receita_ingrediente (
  receita_id bigint not null,
  ingrediente_id bigint not null,
  primary key (receita_id, ingrediente_id),
  foreign key (receita_id) references receitas(id),
  foreign key (ingrediente_id) references ingredientes(id)
);

-- Tabela de Produtos
create table if not exists produtos (
  id bigint primary key,
  descricao varchar(255) not null,
  preco bigint
);

-- Tabela de relacionamento entre Produto e Receita
create table if not exists produto_receita (
  produto_id bigint not null,
  receita_id bigint not null,
  primary key (produto_id,receita_id),
  foreign key (produto_id) references produtos(id),
  foreign key (receita_id) references receitas(id)
);

-- Tabela de Cardapios
create table if not exists cardapios (
  id bigint primary key,
  titulo varchar(255) not null
);

-- Tabela de relacionamento entre Cardapio e Produto
create table if not exists cardapio_produto (
  cardapio_id bigint not null,
  produto_id bigint not null,
  primary key (cardapio_id,produto_id),
  foreign key (cardapio_id) references cardapios(id),
  foreign key (produto_id) references produtos(id)
);

create table if not exists pedidos (
    id bigint primary key,
    cliente_cpf varchar(15) not null,
    data_hora_pagamento timestamp,
    status varchar(50) not null,
    valor double precision not null,
    impostos double precision not null,
    desconto double precision not null,
    valor_cobrado double precision not null,
    data_hora_entrega timestamp,
    foreign key (cliente_cpf) references clientes(cpf)
    );

create table if not exists itens_pedido (
    pedido_id bigint not null,
    produto_id bigint not null,
    quantidade int not null,
    primary key (pedido_id, produto_id),
    foreign key (pedido_id) references pedidos(id),
    foreign key (produto_id) references produtos(id)
    );