create table if not exists ingredientes (
  id bigint primary key,
  descricao varchar(255) not null
);

create table if not exists Estoque(
    id bigint primary key,
    quantidade int,
    ingrediente_id bigint,
    foreign key (ingrediente_id) references ingredientes(id)
);