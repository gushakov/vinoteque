create table public.vins (
    id integer generated by default as identity (start with 1) primary key,
    date date,
    casier int,
    appellation varchar(100),
    annee int,
    pays varchar(100),
    region varchar(100),
    vigneron varchar(100),
    qualite varchar(5),
    stock int,
    prix_btl decimal(100),
    annee_consommation integer,
    commentaire varchar(1000)
);