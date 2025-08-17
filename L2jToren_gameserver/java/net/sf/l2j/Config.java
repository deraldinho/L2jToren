package net.sf.l2j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import net.sf.l2j.commons.config.ExProperties;
import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.gameserver.enums.GeoType;
import net.sf.l2j.gameserver.model.holder.BuffSkillHolder;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

/**
 * Esta classe contém as configurações globais do servidor.<br>
 * Ela possui campos estáticos finais inicializados a partir dos arquivos de configuração.
 */
public final class Config
{
	private Config()
	{
		throw new IllegalStateException("Utility class");
	}
	
	private static final CLogger LOGGER = new CLogger(Config.class.getName());
	
	// Arquivos de configuração
	private static final String CLANS_FILE = "./config/clans.properties";
	private static final String EVENTS_FILE = "./config/events.properties";
	public static final String GEOENGINE_FILE = "./config/geoengine.properties";
	private static final String HEXID_FILE = "./config/hexid.txt";
	private static final String LOGINSERVER_FILE = "./config/loginserver.properties";
	private static final String NPCS_FILE = "./config/npcs.properties";
	private static final String PLAYERS_FILE = "./config/players.properties";
	private static final String SERVER_FILE = "./config/server.properties";
	private static final String SIEGE_FILE = "./config/siege.properties";
	public static final String AUTOFARM_FILE = "./config/autofarm.properties";
	public static final String VoicedCommand_FILE = "./config/VoicedCommand.properties";
	public static final String VIP_FILE = "./config/vip.properties";
	
	// --------------------------------------------------
	// Configurações de Clãs
	// --------------------------------------------------
	
	/** Clãs */
	public static int CLAN_JOIN_DAYS; // Dias para poder entrar em um novo clã
	public static int CLAN_CREATE_DAYS; // Dias para poder criar um novo clã
	public static int CLAN_DISSOLVE_DAYS; // Dias para dissolver um clã
	public static int ALLY_JOIN_DAYS_WHEN_LEAVED; // Dias para entrar em uma nova aliança após sair de uma
	public static int ALLY_JOIN_DAYS_WHEN_DISMISSED; // Dias para entrar em uma nova aliança após ser expulso de uma
	public static int ACCEPT_CLAN_DAYS_WHEN_DISMISSED; // Dias para aceitar um novo clã em uma aliança após expulsar um
	public static int CREATE_ALLY_DAYS_WHEN_DISSOLVED; // Dias para criar uma nova aliança após dissolver uma
	public static int MAX_NUM_OF_CLANS_IN_ALLY; // Número máximo de clãs em uma aliança
	public static int CLAN_MEMBERS_FOR_WAR; // Membros necessários para declarar guerra
	public static int CLAN_WAR_PENALTY_WHEN_ENDED; // Penalidade em dias para declarar guerra novamente ao mesmo clã
	public static boolean MEMBERS_CAN_WITHDRAW_FROM_CLANWH; // Se os membros podem retirar itens do armazém do clã
	
	/** Manor */
	public static int MANOR_REFRESH_TIME; // Hora de atualização do Manor
	public static int MANOR_REFRESH_MIN; // Minuto de atualização do Manor
	public static int MANOR_APPROVE_TIME; // Hora de aprovação do próximo período do Manor
	public static int MANOR_APPROVE_MIN; // Minuto de aprovação do próximo período do Manor
	public static int MANOR_MAINTENANCE_MIN; // Duração da manutenção do Manor em minutos
	public static int MANOR_SAVE_PERIOD_RATE; // Frequência de salvamento do período do Manor em horas
	
	// --------------------------------------------------
	// Configurações de Eventos
	// --------------------------------------------------
	
	/** Olimpíadas */
	public static int OLY_START_TIME; // Hora de início da Olimpíada
	public static int OLY_MIN; // Minuto de início da Olimpíada
	public static long OLY_CPERIOD; // Período de competição da Olimpíada
	public static long OLY_BATTLE; // Duração da batalha da Olimpíada
	public static int OLY_WAIT_TIME; // Tempo de espera antes de ser teleportado para a arena
	public static int OLY_WAIT_BATTLE; // Tempo de espera antes do início da batalha
	public static int OLY_WAIT_END; // Tempo de espera antes de ser teleportado de volta para a cidade
	public static int OLY_START_POINTS; // Pontos iniciais da Olimpíada
	public static int OLY_WEEKLY_POINTS; // Pontos semanais da Olimpíada
	public static int OLY_MIN_MATCHES; // Mínimo de partidas para ser classificado
	public static int OLY_CLASSED; // Mínimo de participantes para jogos com base em classe
	public static int OLY_NONCLASSED; // Mínimo de participantes para jogos sem base em classe
	public static IntIntHolder[] OLY_CLASSED_REWARD; // Recompensa para jogos com base em classe
	public static IntIntHolder[] OLY_NONCLASSED_REWARD; // Recompensa para jogos sem base em classe
	public static int OLY_GP_PER_POINT; // Taxa de troca de pontos por item de recompensa
	public static int OLY_HERO_POINTS; // Pontos de herói para o clã
	public static int OLY_MAX_POINTS; // Máximo de pontos que um jogador pode ganhar/perder em uma partida
	public static int OLY_DIVIDER_CLASSED; // Divisor de pontos em jogos com base em classe
	public static int OLY_DIVIDER_NON_CLASSED; // Divisor de pontos em jogos sem base em classe
	public static boolean OLY_ANNOUNCE_GAMES; // Anunciar o início de cada luta da Olimpíada
	
	/** Festival Seven Signs */
	public static boolean SEVEN_SIGNS_BYPASS_PREREQUISITES; // Se True, qualquer um pode se juntar a qualquer lado do Seven Signs
	public static int FESTIVAL_MIN_PLAYER; // Mínimo de jogadores para participar do Festival
	public static int MAXIMUM_PLAYER_CONTRIB; // Contribuição máxima por jogador durante o festival
	public static long FESTIVAL_MANAGER_START; // Início do gerenciador do festival
	public static long FESTIVAL_LENGTH; // Duração do festival
	public static long FESTIVAL_CYCLE_LENGTH; // Duração do ciclo do festival
	public static long FESTIVAL_FIRST_SPAWN; // Primeiro spawn do festival
	public static long FESTIVAL_FIRST_SWARM; // Primeiro enxame do festival
	public static long FESTIVAL_SECOND_SPAWN; // Segundo spawn do festival
	public static long FESTIVAL_SECOND_SWARM; // Segundo enxame do festival
	public static long FESTIVAL_CHEST_SPAWN; // Spawn do baú do festival
	
	/** Four Sepulchers */
	public static int FS_PARTY_MEMBER_COUNT; // Quantidade de membros no grupo para entrar no Four Sepulchers
	
	/** Dimensional Rift */
	public static int RIFT_MIN_PARTY_SIZE; // Tamanho mínimo do grupo para entrar no Rift
	public static int RIFT_AUTO_JUMPS_TIME_MIN; // Tempo mínimo entre saltos automáticos em minutos
	public static int RIFT_AUTO_JUMPS_TIME_RND; // Tempo aleatório para saltos automáticos em segundos
	public static int RIFT_ENTER_COST_RECRUIT; // Custo de entrada para recrutas
	public static int RIFT_ENTER_COST_SOLDIER; // Custo de entrada para soldados
	public static int RIFT_ENTER_COST_OFFICER; // Custo de entrada para oficiais
	public static int RIFT_ENTER_COST_CAPTAIN; // Custo de entrada para capitães
	public static int RIFT_ENTER_COST_COMMANDER; // Custo de entrada para comandantes
	public static int RIFT_ENTER_COST_HERO; // Custo de entrada para heróis
	public static int RIFT_ANAKAZEL_PORT_CHANCE; // Chance de ser teleportado para o RB Anakazel
	
	/** Loteria */
	public static int LOTTERY_PRIZE; // Prêmio inicial da loteria
	public static int LOTTERY_TICKET_PRICE; // Preço do bilhete da loteria
	public static double LOTTERY_5_NUMBER_RATE; // Taxa para 5 números corretos
	public static double LOTTERY_4_NUMBER_RATE; // Taxa para 4 números corretos
	public static double LOTTERY_3_NUMBER_RATE; // Taxa para 3 números corretos
	public static int LOTTERY_2_AND_1_NUMBER_PRIZE; // Prêmio para 2 ou 1 número correto
	
	/** Torneio de Pesca */
	public static boolean ALLOW_FISH_CHAMPIONSHIP; // Permitir o torneio de pesca
	public static int FISH_CHAMPIONSHIP_REWARD_ITEM; // Item de recompensa do torneio de pesca
	public static int FISH_CHAMPIONSHIP_REWARD_1; // Recompensa para o 1º lugar
	public static int FISH_CHAMPIONSHIP_REWARD_2; // Recompensa para o 2º lugar
	public static int FISH_CHAMPIONSHIP_REWARD_3; // Recompensa para o 3º lugar
	public static int FISH_CHAMPIONSHIP_REWARD_4; // Recompensa para o 4º lugar
	public static int FISH_CHAMPIONSHIP_REWARD_5; // Recompensa para o 5º lugar
	
	// --------------------------------------------------
	// GeoEngine
	// --------------------------------------------------
	
	/** Geodata */
	public static String GEODATA_PATH; // Caminho para os arquivos de geodata
	public static GeoType GEODATA_TYPE; // Tipo de arquivos de geodata
	
	/** Movimento */
	public static int MAX_GEOPATH_FAIL_COUNT; // Máximo de falhas de geopath antes de um aviso
	
	/** Verificação de Caminho */
	public static int PART_OF_CHARACTER_HEIGHT; // Porcentagem da altura do personagem para a linha de visão
	public static int MAX_OBSTACLE_HEIGHT; // Altura máxima de um obstáculo
	
	/** Pathfinding */
	public static int MOVE_WEIGHT; // Peso do movimento axial
	public static int MOVE_WEIGHT_DIAG; // Peso do movimento diagonal
	public static int OBSTACLE_WEIGHT; // Peso de um obstáculo
	public static int OBSTACLE_WEIGHT_DIAG; // Peso de um obstáculo na diagonal
	public static int HEURISTIC_WEIGHT; // Peso da heurística
	public static int MAX_ITERATIONS; // Máximo de iterações do pathfinding
	
	// --------------------------------------------------
	// HexID
	// --------------------------------------------------
	
	public static int SERVER_ID; // ID do servidor
	public static byte[] HEX_ID; // ID hexadecimal do servidor
	
	// --------------------------------------------------
	// Loginserver
	// --------------------------------------------------
	
	public static String LOGINSERVER_HOSTNAME; // Hostname do servidor de login
	public static int LOGINSERVER_PORT; // Porta do servidor de login
	
	public static int LOGIN_TRY_BEFORE_BAN; // Tentativas de login antes de banir o IP
	public static int LOGIN_BLOCK_AFTER_BAN; // Tempo de bloqueio após o banimento
	public static boolean ACCEPT_NEW_GAMESERVER; // Aceitar novos servidores de jogo
	
	public static boolean SHOW_LICENCE; // Mostrar a licença ao fazer login
	
	public static boolean AUTO_CREATE_ACCOUNTS; // Criar contas automaticamente
	
	public static boolean FLOOD_PROTECTION; // Ativar proteção contra flood
	public static int FAST_CONNECTION_LIMIT; // Limite de conexões rápidas
	public static int NORMAL_CONNECTION_TIME; // Tempo de conexão normal
	public static int FAST_CONNECTION_TIME; // Tempo de conexão rápida
	public static int MAX_CONNECTION_PER_IP; // Máximo de conexões por IP
	
	// --------------------------------------------------
	// NPCs / Monstros
	// --------------------------------------------------
	
	/** Spawn */
	public static double SPAWN_MULTIPLIER; // Multiplicador de spawn
	public static String[] SPAWN_EVENTS; // Eventos de spawn
	
	/** Class Master */
	public static boolean ALLOW_ENTIRE_TREE; // Permitir que o Class Master mude para qualquer classe da árvore
	public static ClassMasterSettings CLASS_MASTER_SETTINGS; // Configurações do Class Master
	
	/** Gerenciador de Casamento */
	public static int WEDDING_PRICE; // Preço do casamento
	public static boolean WEDDING_SAMESEX; // Permitir casamento entre o mesmo sexo
	public static boolean WEDDING_FORMALWEAR; // Exigir traje formal para o casamento
	
	/** Buffer de Esquema */
	public static int BUFFER_MAX_SCHEMES; // Máximo de esquemas de buffer por personagem
	public static int BUFFER_STATIC_BUFF_COST; // Custo estático por buff
	public static String FIGHTER_SET; // Lista de buffs para guerreiros
	public static int[] FIGHTER_SET_LIST; // Lista de buffs para guerreiros
	public static String MAGE_SET; // Lista de buffs para magos
	public static int[] MAGE_SET_LIST; // Lista de buffs para magos
	public static int BUFFER_MAX_SKILLS; // Máximo de skills por esquema
	public static String BUFFER_BUFFS; // Lista de buffs do buffer
	public static Map<Integer, BuffSkillHolder> BUFFER_BUFFLIST; // Mapa de buffs do buffer
	public static boolean RESTRICT_USE_BUFFER_ON_PVPFLAG; // Restringir o uso do buffer com a bandeira de PvP
	public static boolean RESTRICT_USE_BUFFER_IN_COMBAT; // Restringir o uso do buffer em combate

	public static List<Integer> FIGHTER_SKILL_LIST; // Lista de skills para guerreiros
	public static List<Integer> MAGE_SKILL_LIST; // Lista de skills para magos

	public static int PVOTE_BUFF_ITEM_ID; // ID do item de recompensa por voto
	public static int PVOTE_BUFF_ITEM_COUNT; // Quantidade do item de recompensa por voto
	
	/** Diversos */
	public static boolean FREE_TELEPORT; // Teleporte gratuito
	public static boolean MOB_AGGRO_IN_PEACEZONE; // Agressividade de monstros em zonas de paz
	public static boolean SHOW_NPC_LVL; // Mostrar o nível dos NPCs
	public static boolean SHOW_NPC_CREST; // Mostrar o emblema do clã nos NPCs
	public static boolean SHOW_SUMMON_CREST; // Mostrar o emblema do clã nos summons
	
	/** Gerenciador de Wyvern */
	public static int WYVERN_REQUIRED_LEVEL; // Nível necessário do Strider para se transformar em Wyvern
	public static int WYVERN_REQUIRED_CRYSTALS; // Cristais necessários para a transformação
	
	/** Raid Boss */
	public static double RAID_HP_REGEN_MULTIPLIER; // Multiplicador de regeneração de HP de Raid Boss
	public static double RAID_MP_REGEN_MULTIPLIER; // Multiplicador de regeneração de MP de Raid Boss
	public static double RAID_DEFENCE_MULTIPLIER; // Multiplicador de defesa de Raid Boss
	
	public static boolean RAID_DISABLE_CURSE; // Desativar a maldição de nível de Raid Boss
	
	/** Grand Boss */
	public static int WAIT_TIME_ANTHARAS; // Tempo de espera para o Antharas
	public static int WAIT_TIME_VALAKAS; // Tempo de espera para o Valakas
	public static int WAIT_TIME_FRINTEZZA; // Tempo de espera para o Frintezza
	
	/** IA */
	public static boolean GUARD_ATTACK_AGGRO_MOB; // Guardas atacam monstros agressivos
	public static int RANDOM_WALK_RATE; // Taxa de caminhada aleatória
	public static int MAX_DRIFT_RANGE; // Distância máxima de deriva do spawn
	public static int DEFAULT_SEE_RANGE; // Alcance de visão padrão
	
	// --------------------------------------------------
	// Jogadores
	// --------------------------------------------------
	
	/** Diversos */
	public static boolean EFFECT_CANCELING; // Cancelar efeitos de menor prioridade
	public static double HP_REGEN_MULTIPLIER; // Multiplicador de regeneração de HP
	public static double MP_REGEN_MULTIPLIER; // Multiplicador de regeneração de MP
	public static double CP_REGEN_MULTIPLIER; // Multiplicador de regeneração de CP
	public static int PLAYER_SPAWN_PROTECTION; // Proteção de spawn do jogador em segundos
	public static int PLAYER_FAKEDEATH_UP_PROTECTION; // Proteção ao se levantar de fake death em segundos
	public static double RESPAWN_RESTORE_HP; // Porcentagem de HP restaurado ao reviver
	public static int MAX_PVTSTORE_SLOTS_DWARF; // Máximo de slots de loja privada para anões
	public static int MAX_PVTSTORE_SLOTS_OTHER; // Máximo de slots de loja privada para outras raças
	public static boolean DEEPBLUE_DROP_RULES; // Regras de drop para monstros "deep blue"
	public static boolean ALLOW_DELEVEL; // Permitir a perda de nível
	public static int DEATH_PENALTY_CHANCE; // Chance de penalidade de morte
	
	/** Inventário e Armazém */
	public static int INVENTORY_MAXIMUM_NO_DWARF; // Máximo de slots de inventário para não anões
	public static int INVENTORY_MAXIMUM_DWARF; // Máximo de slots de inventário para anões
	public static int INVENTORY_MAXIMUM_PET; // Máximo de slots de inventário para pets
	public static int MAX_ITEM_IN_PACKET; // Máximo de itens em um pacote
	public static double WEIGHT_LIMIT; // Limite de peso
	public static int WAREHOUSE_SLOTS_NO_DWARF; // Slots de armazém para não anões
	public static int WAREHOUSE_SLOTS_DWARF; // Slots de armazém para anões
	public static int WAREHOUSE_SLOTS_CLAN; // Slots de armazém do clã
	public static int FREIGHT_SLOTS; // Slots de frete
	public static boolean REGION_BASED_FREIGHT; // Frete baseado em região
	public static int FREIGHT_PRICE; // Preço do frete
	
	/** Encantamento */
	public static double ENCHANT_CHANCE_WEAPON_MAGIC; // Chance de encantar arma mágica
	public static double ENCHANT_CHANCE_WEAPON_MAGIC_15PLUS; // Chance de encantar arma mágica +15 ou mais
	public static double ENCHANT_CHANCE_WEAPON_NONMAGIC; // Chance de encantar arma não mágica
	public static double ENCHANT_CHANCE_WEAPON_NONMAGIC_15PLUS; // Chance de encantar arma não mágica +15 ou mais
	public static double ENCHANT_CHANCE_ARMOR; // Chance de encantar armadura
	public static int ENCHANT_MAX_WEAPON; // Encantamento máximo para armas
	public static int ENCHANT_MAX_ARMOR; // Encantamento máximo para armaduras
	public static int ENCHANT_SAFE_MAX; // Encantamento seguro máximo
	public static int ENCHANT_SAFE_MAX_FULL; // Encantamento seguro máximo para armaduras de corpo inteiro
	
	/** Augmentations */
	public static int AUGMENTATION_NG_SKILL_CHANCE; // Chance de skill em augmentation de baixo grau
	public static int AUGMENTATION_NG_GLOW_CHANCE; // Chance de brilho em augmentation de baixo grau
	public static int AUGMENTATION_MID_SKILL_CHANCE; // Chance de skill em augmentation de grau médio
	public static int AUGMENTATION_MID_GLOW_CHANCE; // Chance de brilho em augmentation de grau médio
	public static int AUGMENTATION_HIGH_SKILL_CHANCE; // Chance de skill em augmentation de alto grau
	public static int AUGMENTATION_HIGH_GLOW_CHANCE; // Chance de brilho em augmentation de alto grau
	public static int AUGMENTATION_TOP_SKILL_CHANCE; // Chance de skill em augmentation de grau máximo
	public static int AUGMENTATION_TOP_GLOW_CHANCE; // Chance de brilho em augmentation de grau máximo
	public static int AUGMENTATION_BASESTAT_CHANCE; // Chance de modificador de status base em augmentation
	
	/** Karma e PvP */
	public static boolean KARMA_PLAYER_CAN_SHOP; // Jogador com karma pode usar loja
	public static boolean KARMA_PLAYER_CAN_USE_GK; // Jogador com karma pode usar Gatekeeper
	public static boolean KARMA_PLAYER_CAN_TELEPORT; // Jogador com karma pode se teleportar
	public static boolean KARMA_PLAYER_CAN_TRADE; // Jogador com karma pode negociar
	public static boolean KARMA_PLAYER_CAN_USE_WH; // Jogador com karma pode usar armazém
	
	public static boolean KARMA_DROP_GM; // GM pode dropar equipamento
	public static boolean KARMA_AWARD_PK_KILL; // Conceder ponto de PvP por matar um jogador com karma
	public static int KARMA_PK_LIMIT; // Limite de PK para dropar itens
	
	public static int[] KARMA_NONDROPPABLE_PET_ITEMS; // Itens de pet que não podem ser dropados
	public static int[] KARMA_NONDROPPABLE_ITEMS; // Itens que não podem ser dropados por PK
	
	public static int PVP_NORMAL_TIME; // Tempo em modo PvP após atacar um inocente
	public static int PVP_PVP_TIME; // Tempo em modo PvP após atacar um jogador em modo PvP
	
	/** Grupo */
	public static String PARTY_XP_CUTOFF_METHOD; // Método de corte de distribuição de XP do grupo
	public static int PARTY_XP_CUTOFF_LEVEL; // Nível de corte de distribuição de XP do grupo
	public static double PARTY_XP_CUTOFF_PERCENT; // Porcentagem de corte de distribuição de XP do grupo
	public static int PARTY_RANGE; // Alcance do grupo
	
	/** GMs e Administração */
	public static int DEFAULT_ACCESS_LEVEL; // Nível de acesso padrão
	public static boolean GM_HERO_AURA; // Aura de herói para GMs
	public static boolean GM_STARTUP_INVULNERABLE; // GM invulnerável ao entrar no jogo
	public static boolean GM_STARTUP_INVISIBLE; // GM invisível ao entrar no jogo
	public static boolean GM_STARTUP_BLOCK_ALL; // Bloquear todas as mensagens privadas para GMs ao entrar no jogo
	public static boolean GM_STARTUP_AUTO_LIST; // Listar GMs automaticamente na lista de GMs ao entrar no jogo
	
	/** Petições */
	public static boolean PETITIONING_ALLOWED; // Permitir petições
	public static int MAX_PETITIONS_PER_PLAYER; // Máximo de petições por jogador
	public static int MAX_PETITIONS_PENDING; // Máximo de petições pendentes
	
	/** Crafting **/
	public static boolean IS_CRAFTING_ENABLED; // Ativar crafting
	public static int DWARF_RECIPE_LIMIT; // Limite de receitas para anões
	public static int COMMON_RECIPE_LIMIT; // Limite de receitas comuns
	public static boolean BLACKSMITH_USE_RECIPES; // Ferreiros usam receitas do inventário do jogador
	
	/** Skills e Classes **/
	public static boolean AUTO_LEARN_SKILLS; // Aprender skills automaticamente
	public static boolean MAGIC_FAILURES; // Falhas em magias
	public static int PERFECT_SHIELD_BLOCK_RATE; // Taxa de bloqueio perfeito com escudo
	public static boolean LIFE_CRYSTAL_NEEDED; // Cristal da vida necessário para aprender skills de clã
	public static boolean SP_BOOK_NEEDED; // Livro de feitiços necessário para aprender skills
	public static boolean ES_SP_BOOK_NEEDED; // Livro de feitiços necessário para encantar skills
	public static boolean DIVINE_SP_BOOK_NEEDED; // Livro de feitiços necessário para aprender Divine Inspiration
	public static boolean SUBCLASS_WITHOUT_QUESTS; // Subclasse sem quests
	
	/** Buffs */
	public static boolean STORE_SKILL_COOLTIME; // Salvar o tempo de recarga das skills ao sair do jogo
	public static int MAX_BUFFS_AMOUNT; // Quantidade máxima de buffs
	
	// --------------------------------------------------
	// Sieges
	// --------------------------------------------------
	
	public static int SIEGE_LENGTH; // Duração da siege em minutos
	public static int MINIMUM_CLAN_LEVEL; // Nível mínimo do clã para registrar na siege
	public static int MAX_ATTACKERS_NUMBER; // Número máximo de clãs atacantes
	public static int MAX_DEFENDERS_NUMBER; // Número máximo de clãs defensores
	public static int ATTACKERS_RESPAWN_DELAY; // Tempo de respawn dos atacantes
	
	public static int CH_MINIMUM_CLAN_LEVEL; // Nível mínimo do clã para registrar na siege de Clan Hall
	public static int CH_MAX_ATTACKERS_NUMBER; // Número máximo de clãs atacantes na siege de Clan Hall
	
	// --------------------------------------------------
	// Servidor
	// --------------------------------------------------
	
	public static String HOSTNAME; // Hostname do servidor
	public static String GAMESERVER_HOSTNAME; // Hostname do servidor de jogo
	public static int GAMESERVER_PORT; // Porta do servidor de jogo
	public static String GAMESERVER_LOGIN_HOSTNAME; // Hostname do servidor de login para o servidor de jogo
	public static int GAMESERVER_LOGIN_PORT; // Porta do servidor de login para o servidor de jogo
	public static int REQUEST_ID; // ID do servidor solicitado
	public static boolean ACCEPT_ALTERNATE_ID; // Aceitar ID alternativo
	public static boolean USE_BLOWFISH_CIPHER; // Usar criptografia Blowfish
	
	/** Acesso ao banco de dados */
	public static String DATABASE_URL; // URL do banco de dados
	public static String DATABASE_LOGIN; // Login do banco de dados
	public static String DATABASE_PASSWORD; // Senha do banco de dados
	
	/** Lista de servidores e Teste */
	public static boolean SERVER_LIST_BRACKET; // Mostrar colchetes no nome do servidor
	public static boolean SERVER_LIST_CLOCK; // Mostrar relógio no nome do servidor
	public static int SERVER_LIST_AGE; // Idade do servidor
	public static boolean SERVER_LIST_TESTSERVER; // Servidor de teste
	public static boolean SERVER_LIST_PVPSERVER; // Servidor PvP
	public static boolean SERVER_GMONLY; // Servidor apenas para GMs
	
	/** Opções relacionadas ao cliente */
	public static int DELETE_DAYS; // Dias para deletar um personagem
	public static int MAXIMUM_ONLINE_USERS; // Máximo de usuários online
	
	/** Auto-loot */
	public static boolean AUTO_LOOT; // Auto-loot
	public static boolean AUTO_LOOT_HERBS; // Auto-loot de ervas
	public static boolean AUTO_LOOT_RAID; // Auto-loot de raid boss
	
	/** Gerenciamento de Itens */
	public static boolean ALLOW_DISCARDITEM; // Permitir descarte de itens
	public static boolean MULTIPLE_ITEM_DROP; // Permitir drop de múltiplos itens não empilháveis
	public static int HERB_AUTO_DESTROY_TIME; // Tempo para destruir ervas dropadas
	public static int ITEM_AUTO_DESTROY_TIME; // Tempo para destruir itens dropados
	public static int EQUIPABLE_ITEM_AUTO_DESTROY_TIME; // Tempo para destruir itens equipáveis dropados
	public static Map<Integer, Integer> SPECIAL_ITEM_DESTROY_TIME; // Tempo para destruir itens especiais dropados
	public static int PLAYER_DROPPED_ITEM_MULTIPLIER; // Multiplicador de tempo para destruir itens dropados por jogadores
	
	/** Taxas */
	public static double RATE_XP; // Taxa de XP
	public static double RATE_SP; // Taxa de SP
	public static double RATE_PARTY_XP; // Taxa de XP em grupo
	public static double RATE_PARTY_SP; // Taxa de SP em grupo
	public static double RATE_DROP_CURRENCY; // Taxa de drop de moeda
	public static double RATE_DROP_ITEMS; // Taxa de drop de itens
	public static double RATE_DROP_ITEMS_BY_RAID; // Taxa de drop de itens por raid boss
	public static double RATE_DROP_SPOIL; // Taxa de drop de spoil
	public static double RATE_DROP_HERBS; // Taxa de drop de ervas
	public static int RATE_DROP_MANOR; // Taxa de drop de manor
	
	public static double RATE_QUEST_DROP; // Taxa de drop de quest
	public static double RATE_QUEST_REWARD; // Taxa de recompensa de quest
	public static double RATE_QUEST_REWARD_XP; // Taxa de recompensa de XP de quest
	public static double RATE_QUEST_REWARD_SP; // Taxa de recompensa de SP de quest
	public static double RATE_QUEST_REWARD_ADENA; // Taxa de recompensa de adena de quest
	
	public static double RATE_KARMA_EXP_LOST; // Taxa de perda de XP com karma
	public static double RATE_SIEGE_GUARDS_PRICE; // Preço dos guardas da siege
	
	public static int PLAYER_DROP_LIMIT; // Limite de drop do jogador
	public static int PLAYER_RATE_DROP; // Taxa de drop do jogador
	public static int PLAYER_RATE_DROP_ITEM; // Taxa de drop de item do jogador
	public static int PLAYER_RATE_DROP_EQUIP; // Taxa de drop de equipamento do jogador
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON; // Taxa de drop de arma do jogador
	
	public static int KARMA_DROP_LIMIT; // Limite de drop com karma
	public static int KARMA_RATE_DROP; // Taxa de drop com karma
	public static int KARMA_RATE_DROP_ITEM; // Taxa de drop de item com karma
	public static int KARMA_RATE_DROP_EQUIP; // Taxa de drop de equipamento com karma
	public static int KARMA_RATE_DROP_EQUIP_WEAPON; // Taxa de drop de arma com karma
	
	public static double PET_XP_RATE; // Taxa de XP de pet
	public static int PET_FOOD_RATE; // Taxa de comida de pet
	public static double SINEATER_XP_RATE; // Taxa de XP de Sin Eater
	
	/** Tipos permitidos */
	public static boolean ALLOW_FREIGHT; // Permitir frete
	public static boolean ALLOW_WAREHOUSE; // Permitir armazém
	public static boolean ALLOW_WEAR; // Permitir experimentar itens
	public static int WEAR_DELAY; // Atraso para experimentar itens
	public static int WEAR_PRICE; // Preço para experimentar itens
	public static boolean ALLOW_LOTTERY; // Permitir loteria
	public static boolean ALLOW_WATER; // Permitir água
	public static boolean ALLOW_MANOR; // Permitir manor
	public static boolean ALLOW_BOAT; // Permitir barco
	public static boolean ALLOW_CURSED_WEAPONS; // Permitir armas amaldiçoadas
	
	public static boolean ENABLE_FALLING_DAMAGE; // Ativar dano de queda
	
	/** Debug e Desenvolvimento */
	public static boolean NO_SPAWNS; // Não carregar spawns
	public static boolean DEVELOPER; // Modo desenvolvedor
	public static boolean PACKET_HANDLER_DEBUG; // Debug do manipulador de pacotes
	
	/** Logs */
	public static boolean LOG_CHAT; // Logar chat
	public static boolean LOG_ITEMS; // Logar itens
	public static boolean GMAUDIT; // Auditoria de GM
	
	/** Community Board */
	public static boolean ENABLE_COMMUNITY_BOARD; // Ativar Community Board
	public static String BBS_DEFAULT; // Seção padrão do Community Board
	
	/** Proteção contra Flood */
	public static int ROLL_DICE_TIME; // Tempo para rolar dados
	public static int HERO_VOICE_TIME; // Tempo para usar a voz de herói
	public static int SUBCLASS_TIME; // Tempo para adicionar subclasse
	public static int DROP_ITEM_TIME; // Tempo para dropar item
	public static int SERVER_BYPASS_TIME; // Tempo para bypass do servidor
	public static int MULTISELL_TIME; // Tempo para multisell
	public static int MANUFACTURE_TIME; // Tempo para manufacture
	public static int MANOR_TIME; // Tempo para manor
	public static int SENDMAIL_TIME; // Tempo para enviar e-mail
	public static int CHARACTER_SELECT_TIME; // Tempo para selecionar personagem
	public static int GLOBAL_CHAT_TIME; // Tempo para chat global
	public static int TRADE_CHAT_TIME; // Tempo para chat de troca
	public static int SOCIAL_TIME; // Tempo para ações sociais
	
	/** Diversos */
	public static boolean L2WALKER_PROTECTION; // Proteção contra L2Walker
	public static boolean SERVER_NEWS; // Notícias do servidor
	public static int ZONE_TOWN; // Configuração da zona da cidade

	/** Auto Farm */
	public static boolean AUTOFARM_ENABLED; // Ativar sistema de auto farm
	public static boolean AUTOFARM_ALLOW_DUALBOX; // Permitir dualbox no auto farm
	public static boolean AUTOFARM_SEND_LOG_MESSAGES; // Enviar mensagens de log do auto farm
	public static boolean AUTOFARM_CHANGE_PLAYER_TITLE; // Alterar o título do jogador no auto farm
	public static boolean AUTOFARM_CHANGE_PLAYER_NAME_COLOR; // Alterar a cor do nome do jogador no auto farm
	public static boolean AUTOFARM_DISABLE_TOWN; // Desativar auto farm na cidade
	public static boolean AUTOFARM_SHOW_ROUTE_RANGE; // Mostrar o alcance da rota no auto farm
	public static double AUTOFARM_HP_HEAL_RATE; // Taxa de cura de HP no auto farm
	public static double AUTOFARM_MP_HEAL_RATE; // Taxa de cura de MP no auto farm
	public static int AUTOFARM_MAX_ZONE_AREA; // Área máxima da zona de auto farm
	public static int AUTOFARM_MAX_ROUTE_PERIMITER; // Perímetro máximo da rota de auto farm
	public static int AUTOFARM_MAX_ZONES; // Máximo de zonas de auto farm
	public static int AUTOFARM_MAX_ROUTES; // Máximo de rotas de auto farm
	public static int AUTOFARM_MAX_ZONE_NODES; // Máximo de nós de zona de auto farm
	public static int AUTOFARM_MAX_ROUTE_NODES; // Máximo de nós de rota de auto farm
	public static int AUTOFARM_DEBUFF_CHANCE; // Chance de debuff no auto farm
	public static int AUTOFARM_MAX_TIMER; // Tempo máximo do auto farm
	public static int AUTOFARM_MAX_OPEN_RADIUS; // Raio máximo de auto farm aberto
	public static int[] AUTOFARM_HP_POTIONS; // Poções de HP para o auto farm
	public static int[] AUTOFARM_MP_POTIONS; // Poções de MP para o auto farm
	public static String AUTOFARM_NAME_COLOR; // Cor do nome do jogador no auto farm


	/** Comandos de Voz */
	public static boolean COMMAND_ONLINE; // Comando .online
    public static boolean BANKING_SYSTEM; // Sistema bancário
    public static int BANKING_SYSTEM_ADENA; // Adena para o sistema bancário
	public static int BANKING_SYSTEM_GOLDBAR; // Barra de ouro para o sistema bancário
	public static IntIntHolder[] BANKING_SYSTEM_ITEM; // Item para o sistema bancário

	/** Sistema VIP */
	public static boolean NEW_CHAR_VIP; // Personagem novo começa como VIP
	public static int DEFAULT_VIP_LEVEL; // Nível VIP padrão
	public static long DEFAULT_VIP_EXP; // Experiência VIP padrão
	public static long DEFAULT_VIP_TIME; // Tempo VIP padrão

	// --------------------------------------------------
	// Essas configurações "ocultas" não têm configs para evitar que os administradores estraguem seu servidor
	// Você ainda pode experimentar alterar os valores aqui. Mas não diga que não avisei.
	// --------------------------------------------------
	
	/** Reserva de Host no LoginServerThread */
	public static boolean RESERVE_HOST_ON_LOGIN = false; // padrão false
	
	/** Configurações de MMO */
	public static int MMO_SELECTOR_SLEEP_TIME = 20; // padrão 20
	public static int MMO_MAX_SEND_PER_PASS = 80; // padrão 80
	public static int MMO_MAX_READ_PER_PASS = 80; // padrão 80
	public static int MMO_HELPER_BUFFER_COUNT = 20; // padrão 20
	
	/** Configurações da Fila de Pacotes do Cliente */
	public static int CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2; // padrão MMO_MAX_READ_PER_PASS + 2
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1; // padrão MMO_MAX_READ_PER_PASS + 1
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = 160; // padrão 160
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = 5; // padrão 5
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = 80; // padrão 80
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = 2; // padrão 2
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = 1; // padrão 1
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = 1; // padrão 1
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = 5; // padrão 5
	
	// --------------------------------------------------
	
	/**
	 * Inicializa {@link ExProperties} a partir do arquivo de configuração especificado.
	 * @param filename : Nome do arquivo a ser carregado.
	 * @return ExProperties : {@link ExProperties} inicializado.
	 */
	public static final ExProperties initProperties(String filename)
	{
		final ExProperties result = new ExProperties();
		
		try
		{
			result.load(new File(filename));
		}
		catch (Exception e)
		{
			LOGGER.error("Ocorreu um erro ao carregar o arquivo de configuração '{}'.", e, filename);
		}
		
		return result;
	}
	
	/**
	 * Carrega as configurações de clãs e clan halls.
	 */
	private static final void loadClans()
	{
		final ExProperties clans = initProperties(CLANS_FILE);
		
		CLAN_JOIN_DAYS = clans.getProperty("DaysBeforeJoinAClan", 5);
		CLAN_CREATE_DAYS = clans.getProperty("DaysBeforeCreateAClan", 10);
		MAX_NUM_OF_CLANS_IN_ALLY = clans.getProperty("MaxNumOfClansInAlly", 3);
		CLAN_MEMBERS_FOR_WAR = clans.getProperty("ClanMembersForWar", 15);
		CLAN_WAR_PENALTY_WHEN_ENDED = clans.getProperty("ClanWarPenaltyWhenEnded", 5);
		CLAN_DISSOLVE_DAYS = clans.getProperty("DaysToPassToDissolveAClan", 7);
		ALLY_JOIN_DAYS_WHEN_LEAVED = clans.getProperty("DaysBeforeJoinAllyWhenLeaved", 1);
		ALLY_JOIN_DAYS_WHEN_DISMISSED = clans.getProperty("DaysBeforeJoinAllyWhenDismissed", 1);
		ACCEPT_CLAN_DAYS_WHEN_DISMISSED = clans.getProperty("DaysBeforeAcceptNewClanWhenDismissed", 1);
		CREATE_ALLY_DAYS_WHEN_DISSOLVED = clans.getProperty("DaysBeforeCreateNewAllyWhenDissolved", 10);
		MEMBERS_CAN_WITHDRAW_FROM_CLANWH = clans.getProperty("MembersCanWithdrawFromClanWH", false);
		
		MANOR_REFRESH_TIME = clans.getProperty("ManorRefreshTime", 20);
		MANOR_REFRESH_MIN = clans.getProperty("ManorRefreshMin", 0);
		MANOR_APPROVE_TIME = clans.getProperty("ManorApproveTime", 6);
		MANOR_APPROVE_MIN = clans.getProperty("ManorApproveMin", 0);
		MANOR_MAINTENANCE_MIN = clans.getProperty("ManorMaintenanceMin", 6);
		MANOR_SAVE_PERIOD_RATE = clans.getProperty("ManorSavePeriodRate", 2) * 3600000;
	}
	
	/**
	 * Carrega as configurações de eventos.<br>
	 * Como olimpíadas, festival seven signs, four sepulchers, dimensional rift, casamentos, loteria, torneio de pesca.
	 */
	private static final void loadEvents()
	{
		final ExProperties events = initProperties(EVENTS_FILE);
		
		OLY_START_TIME = events.getProperty("OlyStartTime", 18);
		OLY_MIN = events.getProperty("OlyMin", 0);
		OLY_CPERIOD = events.getProperty("OlyCPeriod", 21600000L);
		OLY_BATTLE = events.getProperty("OlyBattle", 180000L);
		OLY_WAIT_TIME = events.getProperty("OlyWaitTime", 30);
		OLY_WAIT_BATTLE = events.getProperty("OlyWaitBattle", 60);
		OLY_WAIT_END = events.getProperty("OlyWaitEnd", 40);
		OLY_START_POINTS = events.getProperty("OlyStartPoints", 18);
		OLY_WEEKLY_POINTS = events.getProperty("OlyWeeklyPoints", 3);
		OLY_MIN_MATCHES = events.getProperty("OlyMinMatchesToBeClassed", 5);
		OLY_CLASSED = events.getProperty("OlyClassedParticipants", 5);
		OLY_NONCLASSED = events.getProperty("OlyNonClassedParticipants", 9);
		OLY_CLASSED_REWARD = events.parseIntIntList("OlyClassedReward", "6651-50");
		OLY_NONCLASSED_REWARD = events.parseIntIntList("OlyNonClassedReward", "6651-30");
		OLY_GP_PER_POINT = events.getProperty("OlyGPPerPoint", 1000);
		OLY_HERO_POINTS = events.getProperty("OlyHeroPoints", 300);
		OLY_MAX_POINTS = events.getProperty("OlyMaxPoints", 10);
		OLY_DIVIDER_CLASSED = events.getProperty("OlyDividerClassed", 3);
		OLY_DIVIDER_NON_CLASSED = events.getProperty("OlyDividerNonClassed", 5);
		OLY_ANNOUNCE_GAMES = events.getProperty("OlyAnnounceGames", true);
		
		SEVEN_SIGNS_BYPASS_PREREQUISITES = events.getProperty("SevenSignsBypassPrerequisites", false);
		FESTIVAL_MIN_PLAYER = Math.clamp(events.getProperty("FestivalMinPlayer", 5), 2, 9);
		MAXIMUM_PLAYER_CONTRIB = events.getProperty("MaxPlayerContrib", 1000000);
		FESTIVAL_MANAGER_START = events.getProperty("FestivalManagerStart", 120000L);
		FESTIVAL_LENGTH = events.getProperty("FestivalLength", 1080000L);
		FESTIVAL_CYCLE_LENGTH = events.getProperty("FestivalCycleLength", 2280000L);
		FESTIVAL_FIRST_SPAWN = events.getProperty("FestivalFirstSpawn", 120000L);
		FESTIVAL_FIRST_SWARM = events.getProperty("FestivalFirstSwarm", 300000L);
		FESTIVAL_SECOND_SPAWN = events.getProperty("FestivalSecondSpawn", 540000L);
		FESTIVAL_SECOND_SWARM = events.getProperty("FestivalSecondSwarm", 720000L);
		FESTIVAL_CHEST_SPAWN = events.getProperty("FestivalChestSpawn", 900000L);
		
		FS_PARTY_MEMBER_COUNT = Math.clamp(events.getProperty("NeededPartyMembers", 4), 2, 9);
		
		RIFT_MIN_PARTY_SIZE = events.getProperty("RiftMinPartySize", 2);
		RIFT_AUTO_JUMPS_TIME_MIN = events.getProperty("AutoJumpsDelayMin", 8);
		RIFT_AUTO_JUMPS_TIME_RND = events.getProperty("AutoJumpsDelayRnd", 5);
		RIFT_ENTER_COST_RECRUIT = events.getProperty("RecruitCost", 21);
		RIFT_ENTER_COST_SOLDIER = events.getProperty("SoldierCost", 24);
		RIFT_ENTER_COST_OFFICER = events.getProperty("OfficerCost", 27);
		RIFT_ENTER_COST_CAPTAIN = events.getProperty("CaptainCost", 30);
		RIFT_ENTER_COST_COMMANDER = events.getProperty("CommanderCost", 33);
		RIFT_ENTER_COST_HERO = events.getProperty("HeroCost", 36);
		RIFT_ANAKAZEL_PORT_CHANCE = events.getProperty("AnakazelPortChance", 15);
		
		LOTTERY_PRIZE = events.getProperty("LotteryPrize", 50000);
		LOTTERY_TICKET_PRICE = events.getProperty("LotteryTicketPrice", 2000);
		LOTTERY_5_NUMBER_RATE = events.getProperty("Lottery5NumberRate", 0.6);
		LOTTERY_4_NUMBER_RATE = events.getProperty("Lottery4NumberRate", 0.2);
		LOTTERY_3_NUMBER_RATE = events.getProperty("Lottery3NumberRate", 0.2);
		LOTTERY_2_AND_1_NUMBER_PRIZE = events.getProperty("Lottery2and1NumberPrize", 200);
		
		ALLOW_FISH_CHAMPIONSHIP = events.getProperty("AllowFishChampionship", true);
		FISH_CHAMPIONSHIP_REWARD_ITEM = events.getProperty("FishChampionshipRewardItemId", 57);
		FISH_CHAMPIONSHIP_REWARD_1 = events.getProperty("FishChampionshipReward1", 800000);
		FISH_CHAMPIONSHIP_REWARD_2 = events.getProperty("FishChampionshipReward2", 500000);
		FISH_CHAMPIONSHIP_REWARD_3 = events.getProperty("FishChampionshipReward3", 300000);
		FISH_CHAMPIONSHIP_REWARD_4 = events.getProperty("FishChampionshipReward4", 200000);
		FISH_CHAMPIONSHIP_REWARD_5 = events.getProperty("FishChampionshipReward5", 100000);
	}
	
	/**
	 * Carrega as configurações do geoengine.
	 */
	private static final void loadGeoengine()
	{
		final ExProperties geoengine = initProperties(GEOENGINE_FILE);
		
		GEODATA_PATH = geoengine.getProperty("GeoDataPath", "./data/geodata/");
		GEODATA_TYPE = Enum.valueOf(GeoType.class, geoengine.getProperty("GeoDataType", "L2OFF"));
		
		MAX_GEOPATH_FAIL_COUNT = Math.max(15, geoengine.getProperty("MaxGeopathFailCount", 50));
		
		PART_OF_CHARACTER_HEIGHT = geoengine.getProperty("PartOfCharacterHeight", 75);
		MAX_OBSTACLE_HEIGHT = geoengine.getProperty("MaxObstacleHeight", 32);
		
		MOVE_WEIGHT = geoengine.getProperty("MoveWeight", 10);
		MOVE_WEIGHT_DIAG = geoengine.getProperty("MoveWeightDiag", 14);
		OBSTACLE_WEIGHT = geoengine.getProperty("ObstacleWeight", 30);
		OBSTACLE_WEIGHT_DIAG = (int) (OBSTACLE_WEIGHT * Math.sqrt(2));
		HEURISTIC_WEIGHT = geoengine.getProperty("HeuristicWeight", 12);
		MAX_ITERATIONS = geoengine.getProperty("MaxIterations", 10000);
	}
	
	/**
	 * Carrega as configurações do hex ID.
	 */
	private static final void loadHexID()
	{
		final ExProperties hexid = initProperties(HEXID_FILE);
		
		SERVER_ID = Integer.parseInt(hexid.getProperty("ServerID"));
		HEX_ID = new BigInteger(hexid.getProperty("HexID"), 16).toByteArray();
	}
	
	/**
	 * Salva o arquivo hex ID.
	 * @param serverId : O ID do servidor.
	 * @param hexId : O hex ID do servidor.
	 */
	public static final void saveHexid(int serverId, String hexId)
	{
		saveHexid(serverId, hexId, HEXID_FILE);
	}
	
	/**
	 * Salva o arquivo hexID.
	 * @param serverId : O ID do servidor.
	 * @param hexId : O hexID do servidor.
	 * @param filename : O nome do arquivo.
	 */
	public static final void saveHexid(int serverId, String hexId, String filename)
	{
		try
		{
			final File file = new File(filename);
			file.createNewFile();
			
			final Properties hexSetting = new Properties();
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			
			try (OutputStream out = new FileOutputStream(file))
			{
				hexSetting.store(out, "the hexID to auth into login");
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Falha ao salvar o hex ID no arquivo '{}'.", e, filename);
		}
	}
	
	/**
	 * Carrega as configurações de NPCs.<br>
	 * Como monstros campeões, buffer de NPC, class master, wyvern, raid bosses e grand bosses, IA.
	 */
	private static final void loadNpcs()
	{
		final ExProperties npcs = initProperties(NPCS_FILE);
		
		SPAWN_MULTIPLIER = npcs.getProperty("SpawnMultiplier", 1.);
		SPAWN_EVENTS = npcs.getProperty("SpawnEvents", new String[]
		{
			"extra_mob",
			"18age",
			"start_weapon",
		});
		
		ALLOW_ENTIRE_TREE = npcs.getProperty("AllowEntireTree", false);
		CLASS_MASTER_SETTINGS = new ClassMasterSettings(npcs.getProperty("ConfigClassMaster"));
		
		WEDDING_PRICE = npcs.getProperty("WeddingPrice", 1000000);
		WEDDING_SAMESEX = npcs.getProperty("WeddingAllowSameSex", false);
		WEDDING_FORMALWEAR = npcs.getProperty("WeddingFormalWear", true);
		// inicio buff schema
		BUFFER_MAX_SCHEMES = npcs.getProperty("BufferMaxSchemesPerChar", 4);
		BUFFER_STATIC_BUFF_COST = npcs.getProperty("BufferStaticCostPerBuff", -1);

		BUFFER_MAX_SKILLS = npcs.getProperty("BufferMaxSkillsPerScheme", 24);
		BUFFER_BUFFS = npcs.getProperty("BufferBuffs");

		FIGHTER_SET = npcs.getProperty("FighterSet", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
		MAGE_SET = npcs.getProperty("MageSet", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");

		String[] FighterList = FIGHTER_SET.split(",");
		FIGHTER_SET_LIST = new int[FighterList.length];
		for (int i = 0; i < FighterList.length; i++)
			FIGHTER_SET_LIST[i] = Integer.parseInt(FighterList[i]);

		String[] MageList = MAGE_SET.split(",");
		MAGE_SET_LIST = new int[MageList.length];
		for (int i = 0; i < MageList.length; i++)
			MAGE_SET_LIST[i] = Integer.parseInt(MageList[i]);

		BUFFER_BUFFLIST = new HashMap<>();
		for (String skillInfo : BUFFER_BUFFS.split(";"))
		{
			final String[] infos = skillInfo.split(",");
			BUFFER_BUFFLIST.put(Integer.valueOf(infos[0]), new BuffSkillHolder(Integer.valueOf(infos[0]), Integer.valueOf(infos[1]), Integer.valueOf(infos[2]), infos[3], skillInfo));
		}

		RESTRICT_USE_BUFFER_ON_PVPFLAG = npcs.getProperty("RestrictUseBufferOnPvPFlag", true);
		RESTRICT_USE_BUFFER_IN_COMBAT = npcs.getProperty("RestrictUseBufferInCombat", true);

		PVOTE_BUFF_ITEM_ID = npcs.getProperty("VoteBuffItemId", 57);
		PVOTE_BUFF_ITEM_COUNT = npcs.getProperty("VoteBuffItemCount", 1);

		FIGHTER_SKILL_LIST = new ArrayList<>();
		for (String skill_id : npcs.getProperty("FighterSkillList", "").split(";"))
			FIGHTER_SKILL_LIST.add(Integer.parseInt(skill_id));

		MAGE_SKILL_LIST = new ArrayList<>();
		for (String skill_id : npcs.getProperty("MageSkillList", "").split(";"))
			MAGE_SKILL_LIST.add(Integer.parseInt(skill_id));

		// fim buff schema
		FREE_TELEPORT = npcs.getProperty("FreeTeleport", false);
		MOB_AGGRO_IN_PEACEZONE = npcs.getProperty("MobAggroInPeaceZone", true);
		SHOW_NPC_LVL = npcs.getProperty("ShowNpcLevel", false);
		SHOW_NPC_CREST = npcs.getProperty("ShowNpcCrest", false);
		SHOW_SUMMON_CREST = npcs.getProperty("ShowSummonCrest", false);
		
		WYVERN_REQUIRED_LEVEL = npcs.getProperty("RequiredStriderLevel", 55);
		WYVERN_REQUIRED_CRYSTALS = npcs.getProperty("RequiredCrystalsNumber", 10);
		
		RAID_HP_REGEN_MULTIPLIER = npcs.getProperty("RaidHpRegenMultiplier", 1.);
		RAID_MP_REGEN_MULTIPLIER = npcs.getProperty("RaidMpRegenMultiplier", 1.);
		RAID_DEFENCE_MULTIPLIER = npcs.getProperty("RaidDefenceMultiplier", 1.);
		
		RAID_DISABLE_CURSE = npcs.getProperty("DisableRaidCurse", false);
		
		WAIT_TIME_ANTHARAS = npcs.getProperty("AntharasWaitTime", 30) * 60000;
		WAIT_TIME_VALAKAS = npcs.getProperty("ValakasWaitTime", 20) * 60000;
		WAIT_TIME_FRINTEZZA = npcs.getProperty("FrintezzaWaitTime", 10) * 60000;
		
		GUARD_ATTACK_AGGRO_MOB = npcs.getProperty("GuardAttackAggroMob", false);
		RANDOM_WALK_RATE = npcs.getProperty("RandomWalkRate", 30);
		MAX_DRIFT_RANGE = npcs.getProperty("MaxDriftRange", 200);
		DEFAULT_SEE_RANGE = npcs.getProperty("DefaultSeeRange", 450);
	}
	
	/**
	 * Carrega as configurações do jogador.<br>
	 * Como status, inventário/armazém, encantamento, augmentation, karma, grupo, admin, petição, aprendizado de skill.
	 */
	private static final void loadPlayers()
	{
		final ExProperties players = initProperties(PLAYERS_FILE);
		
		EFFECT_CANCELING = players.getProperty("CancelLesserEffect", true);
		HP_REGEN_MULTIPLIER = players.getProperty("HpRegenMultiplier", 1.);
		MP_REGEN_MULTIPLIER = players.getProperty("MpRegenMultiplier", 1.);
		CP_REGEN_MULTIPLIER = players.getProperty("CpRegenMultiplier", 1.);
		PLAYER_SPAWN_PROTECTION = players.getProperty("PlayerSpawnProtection", 0);
		PLAYER_FAKEDEATH_UP_PROTECTION = players.getProperty("PlayerFakeDeathUpProtection", 5);
		RESPAWN_RESTORE_HP = players.getProperty("RespawnRestoreHP", 0.7);
		MAX_PVTSTORE_SLOTS_DWARF = players.getProperty("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = players.getProperty("MaxPvtStoreSlotsOther", 4);
		DEEPBLUE_DROP_RULES = players.getProperty("UseDeepBlueDropRules", true);
		ALLOW_DELEVEL = players.getProperty("AllowDelevel", true);
		DEATH_PENALTY_CHANCE = players.getProperty("DeathPenaltyChance", 20);
		
		INVENTORY_MAXIMUM_NO_DWARF = players.getProperty("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = players.getProperty("MaximumSlotsForDwarf", 100);
		INVENTORY_MAXIMUM_PET = players.getProperty("MaximumSlotsForPet", 12);
		MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, INVENTORY_MAXIMUM_DWARF);
		WEIGHT_LIMIT = players.getProperty("WeightLimit", 1.);
		WAREHOUSE_SLOTS_NO_DWARF = players.getProperty("MaximumWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_DWARF = players.getProperty("MaximumWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_CLAN = players.getProperty("MaximumWarehouseSlotsForClan", 150);
		FREIGHT_SLOTS = players.getProperty("MaximumFreightSlots", 20);
		REGION_BASED_FREIGHT = players.getProperty("RegionBasedFreight", true);
		FREIGHT_PRICE = players.getProperty("FreightPrice", 1000);
		
		ENCHANT_CHANCE_WEAPON_MAGIC = players.getProperty("EnchantChanceMagicWeapon", 0.4);
		ENCHANT_CHANCE_WEAPON_MAGIC_15PLUS = players.getProperty("EnchantChanceMagicWeapon15Plus", 0.2);
		ENCHANT_CHANCE_WEAPON_NONMAGIC = players.getProperty("EnchantChanceNonMagicWeapon", 0.7);
		ENCHANT_CHANCE_WEAPON_NONMAGIC_15PLUS = players.getProperty("EnchantChanceNonMagicWeapon15Plus", 0.35);
		ENCHANT_CHANCE_ARMOR = players.getProperty("EnchantChanceArmor", 0.66);
		ENCHANT_MAX_WEAPON = players.getProperty("EnchantMaxWeapon", 0);
		ENCHANT_MAX_ARMOR = players.getProperty("EnchantMaxArmor", 0);
		ENCHANT_SAFE_MAX = players.getProperty("EnchantSafeMax", 3);
		ENCHANT_SAFE_MAX_FULL = players.getProperty("EnchantSafeMaxFull", 4);
		
		AUGMENTATION_NG_SKILL_CHANCE = players.getProperty("AugmentationNGSkillChance", 15);
		AUGMENTATION_NG_GLOW_CHANCE = players.getProperty("AugmentationNGGlowChance", 0);
		AUGMENTATION_MID_SKILL_CHANCE = players.getProperty("AugmentationMidSkillChance", 30);
		AUGMENTATION_MID_GLOW_CHANCE = players.getProperty("AugmentationMidGlowChance", 40);
		AUGMENTATION_HIGH_SKILL_CHANCE = players.getProperty("AugmentationHighSkillChance", 45);
		AUGMENTATION_HIGH_GLOW_CHANCE = players.getProperty("AugmentationHighGlowChance", 70);
		AUGMENTATION_TOP_SKILL_CHANCE = players.getProperty("AugmentationTopSkillChance", 60);
		AUGMENTATION_TOP_GLOW_CHANCE = players.getProperty("AugmentationTopGlowChance", 100);
		AUGMENTATION_BASESTAT_CHANCE = players.getProperty("AugmentationBaseStatChance", 1);
		
		KARMA_PLAYER_CAN_SHOP = players.getProperty("KarmaPlayerCanShop", false);
		KARMA_PLAYER_CAN_USE_GK = players.getProperty("KarmaPlayerCanUseGK", false);
		KARMA_PLAYER_CAN_TELEPORT = players.getProperty("KarmaPlayerCanTeleport", true);
		KARMA_PLAYER_CAN_TRADE = players.getProperty("KarmaPlayerCanTrade", true);
		KARMA_PLAYER_CAN_USE_WH = players.getProperty("KarmaPlayerCanUseWareHouse", true);
		KARMA_DROP_GM = players.getProperty("CanGMDropEquipment", false);
		KARMA_AWARD_PK_KILL = players.getProperty("AwardPKKillPVPPoint", true);
		KARMA_PK_LIMIT = players.getProperty("MinimumPKRequiredToDrop", 5);
		KARMA_NONDROPPABLE_PET_ITEMS = players.getProperty("ListOfPetItems", new int[]
		{
			2375,
			3500,
			3501,
			3502,
			4422,
			4423,
			4424,
			4425,
			6648,
			6649,
			6650
		});
		KARMA_NONDROPPABLE_ITEMS = players.getProperty("ListOfNonDroppableItemsForPK", new int[]
		{
			1147,
			425,
			1146,
			461,
			10,
			2368,
			7,
			6,
			2370,
			2369
		});
		
		PVP_NORMAL_TIME = players.getProperty("PvPVsNormalTime", 40000);
		PVP_PVP_TIME = players.getProperty("PvPVsPvPTime", 20000);
		
		PARTY_XP_CUTOFF_METHOD = players.getProperty("PartyXpCutoffMethod", "level");
		PARTY_XP_CUTOFF_PERCENT = players.getProperty("PartyXpCutoffPercent", 3.);
		PARTY_XP_CUTOFF_LEVEL = players.getProperty("PartyXpCutoffLevel", 20);
		PARTY_RANGE = players.getProperty("PartyRange", 1500);
		
		DEFAULT_ACCESS_LEVEL = players.getProperty("DefaultAccessLevel", 0);
		GM_HERO_AURA = players.getProperty("GMHeroAura", false);
		GM_STARTUP_INVULNERABLE = players.getProperty("GMStartupInvulnerable", false);
		GM_STARTUP_INVISIBLE = players.getProperty("GMStartupInvisible", false);
		GM_STARTUP_BLOCK_ALL = players.getProperty("GMStartupBlockAll", false);
		GM_STARTUP_AUTO_LIST = players.getProperty("GMStartupAutoList", true);
		
		PETITIONING_ALLOWED = players.getProperty("PetitioningAllowed", true);
		MAX_PETITIONS_PER_PLAYER = players.getProperty("MaxPetitionsPerPlayer", 5);
		MAX_PETITIONS_PENDING = players.getProperty("MaxPetitionsPending", 25);
		
		IS_CRAFTING_ENABLED = players.getProperty("CraftingEnabled", true);
		DWARF_RECIPE_LIMIT = players.getProperty("DwarfRecipeLimit", 50);
		COMMON_RECIPE_LIMIT = players.getProperty("CommonRecipeLimit", 50);
		BLACKSMITH_USE_RECIPES = players.getProperty("BlacksmithUseRecipes", true);
		
		AUTO_LEARN_SKILLS = players.getProperty("AutoLearnSkills", false);
		MAGIC_FAILURES = players.getProperty("MagicFailures", true);
		PERFECT_SHIELD_BLOCK_RATE = players.getProperty("PerfectShieldBlockRate", 5);
		LIFE_CRYSTAL_NEEDED = players.getProperty("LifeCrystalNeeded", true);
		SP_BOOK_NEEDED = players.getProperty("SpBookNeeded", true);
		ES_SP_BOOK_NEEDED = players.getProperty("EnchantSkillSpBookNeeded", true);
		DIVINE_SP_BOOK_NEEDED = players.getProperty("DivineInspirationSpBookNeeded", true);
		SUBCLASS_WITHOUT_QUESTS = players.getProperty("SubClassWithoutQuests", false);
		
		MAX_BUFFS_AMOUNT = players.getProperty("MaxBuffsAmount", 20);
		STORE_SKILL_COOLTIME = players.getProperty("StoreSkillCooltime", true);
	}
	
	/**
	 * Carrega as configurações de siege.
	 */
	private static final void loadSieges()
	{
		final ExProperties sieges = initProperties(Config.SIEGE_FILE);
		
		SIEGE_LENGTH = sieges.getProperty("SiegeLength", 120);
		MINIMUM_CLAN_LEVEL = sieges.getProperty("SiegeClanMinLevel", 4);
		MAX_ATTACKERS_NUMBER = sieges.getProperty("AttackerMaxClans", 10);
		MAX_DEFENDERS_NUMBER = sieges.getProperty("DefenderMaxClans", 10);
		ATTACKERS_RESPAWN_DELAY = sieges.getProperty("AttackerRespawn", 10000);
		
		CH_MINIMUM_CLAN_LEVEL = sieges.getProperty("ChSiegeClanMinLevel", 4);
		CH_MAX_ATTACKERS_NUMBER = sieges.getProperty("ChAttackerMaxClans", 10);
	}
	
	/**
	 * Carrega as configurações do servidor de jogo.<br>
	 * Endereços IP, banco de dados, taxas, recursos ativados/desativados, diversos.
	 */
	private static final void loadServer()
	{
		final ExProperties server = initProperties(SERVER_FILE);
		
		HOSTNAME = server.getProperty("Hostname", "*");
		GAMESERVER_HOSTNAME = server.getProperty("GameserverHostname");
		GAMESERVER_PORT = server.getProperty("GameserverPort", 7777);
		GAMESERVER_LOGIN_HOSTNAME = server.getProperty("LoginHost", "127.0.0.1");
		GAMESERVER_LOGIN_PORT = server.getProperty("LoginPort", 9014);
		REQUEST_ID = server.getProperty("RequestServerID", 0);
		ACCEPT_ALTERNATE_ID = server.getProperty("AcceptAlternateID", true);
		USE_BLOWFISH_CIPHER = server.getProperty("UseBlowfishCipher", true);
		
		DATABASE_URL = server.getProperty("URL", "jdbc:mariadb://localhost/L2jToren");
		DATABASE_LOGIN = server.getProperty("Login", "root");
		DATABASE_PASSWORD = server.getProperty("Password", "");
		
		SERVER_LIST_BRACKET = server.getProperty("ServerListBrackets", false);
		SERVER_LIST_CLOCK = server.getProperty("ServerListClock", false);
		SERVER_GMONLY = server.getProperty("ServerGMOnly", false);
		SERVER_LIST_AGE = server.getProperty("ServerListAgeLimit", 0);
		SERVER_LIST_TESTSERVER = server.getProperty("TestServer", false);
		SERVER_LIST_PVPSERVER = server.getProperty("PvpServer", true);
		
		DELETE_DAYS = server.getProperty("DeleteCharAfterDays", 7);
		MAXIMUM_ONLINE_USERS = server.getProperty("MaximumOnlineUsers", 100);
		
		AUTO_LOOT = server.getProperty("AutoLoot", false);
		AUTO_LOOT_HERBS = server.getProperty("AutoLootHerbs", false);
		AUTO_LOOT_RAID = server.getProperty("AutoLootRaid", false);
		
		ALLOW_DISCARDITEM = server.getProperty("AllowDiscardItem", true);
		MULTIPLE_ITEM_DROP = server.getProperty("MultipleItemDrop", true);
		HERB_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyHerbTime", 15) * 1000;
		ITEM_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyItemTime", 600) * 1000;
		EQUIPABLE_ITEM_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyEquipableItemTime", 0) * 1000;
		SPECIAL_ITEM_DESTROY_TIME = new HashMap<>();
		String[] data = server.getProperty("AutoDestroySpecialItemTime", (String[]) null, ",");
		if (data != null)
		{
			for (String itemData : data)
			{
				String[] item = itemData.split("-");
				SPECIAL_ITEM_DESTROY_TIME.put(Integer.parseInt(item[0]), Integer.parseInt(item[1]) * 1000);
			}
		}
		PLAYER_DROPPED_ITEM_MULTIPLIER = server.getProperty("PlayerDroppedItemMultiplier", 1);
		
		RATE_XP = server.getProperty("RateXp", 1.);
		RATE_SP = server.getProperty("RateSp", 1.);
		RATE_PARTY_XP = server.getProperty("RatePartyXp", 1.);
		RATE_PARTY_SP = server.getProperty("RatePartySp", 1.);
		RATE_DROP_CURRENCY = server.getProperty("RateDropCurrency", 1.);
		RATE_DROP_ITEMS = server.getProperty("RateDropItems", 1.);
		RATE_DROP_ITEMS_BY_RAID = server.getProperty("RateRaidDropItems", 1.);
		RATE_DROP_SPOIL = server.getProperty("RateDropSpoil", 1.);
		RATE_DROP_HERBS = server.getProperty("RateDropHerbs", 1.);
		RATE_DROP_MANOR = server.getProperty("RateDropManor", 1);
		RATE_QUEST_DROP = server.getProperty("RateQuestDrop", 1.);
		RATE_QUEST_REWARD = server.getProperty("RateQuestReward", 1.);
		RATE_QUEST_REWARD_XP = server.getProperty("RateQuestRewardXP", 1.);
		RATE_QUEST_REWARD_SP = server.getProperty("RateQuestRewardSP", 1.);
		RATE_QUEST_REWARD_ADENA = server.getProperty("RateQuestRewardAdena", 1.);
		RATE_KARMA_EXP_LOST = server.getProperty("RateKarmaExpLost", 1.);
		RATE_SIEGE_GUARDS_PRICE = server.getProperty("RateSiegeGuardsPrice", 1.);
		PLAYER_DROP_LIMIT = server.getProperty("PlayerDropLimit", 3);
		PLAYER_RATE_DROP = server.getProperty("PlayerRateDrop", 5);
		PLAYER_RATE_DROP_ITEM = server.getProperty("PlayerRateDropItem", 70);
		PLAYER_RATE_DROP_EQUIP = server.getProperty("PlayerRateDropEquip", 25);
		PLAYER_RATE_DROP_EQUIP_WEAPON = server.getProperty("PlayerRateDropEquipWeapon", 5);
		PET_XP_RATE = server.getProperty("PetXpRate", 1.);
		PET_FOOD_RATE = server.getProperty("PetFoodRate", 1);
		SINEATER_XP_RATE = server.getProperty("SinEaterXpRate", 1.);
		KARMA_DROP_LIMIT = server.getProperty("KarmaDropLimit", 10);
		KARMA_RATE_DROP = server.getProperty("KarmaRateDrop", 70);
		KARMA_RATE_DROP_ITEM = server.getProperty("KarmaRateDropItem", 50);
		KARMA_RATE_DROP_EQUIP = server.getProperty("KarmaRateDropEquip", 40);
		KARMA_RATE_DROP_EQUIP_WEAPON = server.getProperty("KarmaRateDropEquipWeapon", 10);
		
		ALLOW_FREIGHT = server.getProperty("AllowFreight", true);
		ALLOW_WAREHOUSE = server.getProperty("AllowWarehouse", true);
		ALLOW_WEAR = server.getProperty("AllowWear", true);
		WEAR_DELAY = server.getProperty("WearDelay", 5);
		WEAR_PRICE = server.getProperty("WearPrice", 10);
		ALLOW_LOTTERY = server.getProperty("AllowLottery", true);
		ALLOW_WATER = server.getProperty("AllowWater", true);
		ALLOW_MANOR = server.getProperty("AllowManor", true);
		ALLOW_BOAT = server.getProperty("AllowBoat", true);
		ALLOW_CURSED_WEAPONS = server.getProperty("AllowCursedWeapons", true);
		
		ENABLE_FALLING_DAMAGE = server.getProperty("EnableFallingDamage", true);
		
		NO_SPAWNS = server.getProperty("NoSpawns", false);
		DEVELOPER = server.getProperty("Developer", false);
		PACKET_HANDLER_DEBUG = server.getProperty("PacketHandlerDebug", false);
		
		LOG_CHAT = server.getProperty("LogChat", false);
		LOG_ITEMS = server.getProperty("LogItems", false);
		GMAUDIT = server.getProperty("GMAudit", false);
		
		ENABLE_COMMUNITY_BOARD = server.getProperty("EnableCommunityBoard", false);
		BBS_DEFAULT = server.getProperty("BBSDefault", "_bbshome");
		
		ROLL_DICE_TIME = server.getProperty("RollDiceTime", 4200);
		HERO_VOICE_TIME = server.getProperty("HeroVoiceTime", 10000);
		SUBCLASS_TIME = server.getProperty("SubclassTime", 2000);
		DROP_ITEM_TIME = server.getProperty("DropItemTime", 1000);
		SERVER_BYPASS_TIME = server.getProperty("ServerBypassTime", 100);
		MULTISELL_TIME = server.getProperty("MultisellTime", 100);
		MANUFACTURE_TIME = server.getProperty("ManufactureTime", 300);
		MANOR_TIME = server.getProperty("ManorTime", 3000);
		SENDMAIL_TIME = server.getProperty("SendMailTime", 10000);
		CHARACTER_SELECT_TIME = server.getProperty("CharacterSelectTime", 3000);
		GLOBAL_CHAT_TIME = server.getProperty("GlobalChatTime", 0);
		TRADE_CHAT_TIME = server.getProperty("TradeChatTime", 0);
		SOCIAL_TIME = server.getProperty("SocialTime", 2000);
		
		L2WALKER_PROTECTION = server.getProperty("L2WalkerProtection", false);
		ZONE_TOWN = server.getProperty("ZoneTown", 0);
		SERVER_NEWS = server.getProperty("ShowServerNews", false);
	}
	
	/**
	 * Carrega as configurações do servidor de login.<br>
	 * Endereços IP, banco de dados, conta, diversos.
	 */
	private static final void loadLogin()
	{
		final ExProperties server = initProperties(LOGINSERVER_FILE);
		
		HOSTNAME = server.getProperty("Hostname", "localhost");
		LOGINSERVER_HOSTNAME = server.getProperty("LoginserverHostname", "*");
		LOGINSERVER_PORT = server.getProperty("LoginserverPort", 2106);
		GAMESERVER_LOGIN_HOSTNAME = server.getProperty("LoginHostname", "*");
		GAMESERVER_LOGIN_PORT = server.getProperty("LoginPort", 9014);
		LOGIN_TRY_BEFORE_BAN = server.getProperty("LoginTryBeforeBan", 3);
		LOGIN_BLOCK_AFTER_BAN = server.getProperty("LoginBlockAfterBan", 600);
		ACCEPT_NEW_GAMESERVER = server.getProperty("AcceptNewGameServer", false);
		SHOW_LICENCE = server.getProperty("ShowLicence", true);
		
		DATABASE_URL = server.getProperty("URL", "jdbc:mariadb://localhost:3306/L2jToren");
		DATABASE_LOGIN = server.getProperty("Login", "root");
		DATABASE_PASSWORD = server.getProperty("Password", "");
		
		AUTO_CREATE_ACCOUNTS = server.getProperty("AutoCreateAccounts", true);
		
		FLOOD_PROTECTION = server.getProperty("EnableFloodProtection", true);
		FAST_CONNECTION_LIMIT = server.getProperty("FastConnectionLimit", 15);
		NORMAL_CONNECTION_TIME = server.getProperty("NormalConnectionTime", 700);
		FAST_CONNECTION_TIME = server.getProperty("FastConnectionTime", 350);
		MAX_CONNECTION_PER_IP = server.getProperty("MaxConnectionPerIP", 50);
	}

	//Carrega o AutoFarmManager
	public static final void loadAutoFarmManager()
	{
	final ExProperties farm = initProperties(AUTOFARM_FILE);
		AUTOFARM_ENABLED = farm.getProperty("ENABLED", false);
		AUTOFARM_MAX_ZONE_AREA = farm.getProperty("MAX_ZONE_AREA", 7000000);
		AUTOFARM_MAX_ROUTE_PERIMITER = farm.getProperty("MAX_ROUTE_PERIMETER", 7000000);
		AUTOFARM_MAX_OPEN_RADIUS = farm.getProperty("MAX_OPEN_RADIUS", 0);
		AUTOFARM_MAX_ZONES = farm.getProperty("MAX_ZONES", 5);
		AUTOFARM_MAX_ROUTES = farm.getProperty("MAX_ROUTES", 5);
		AUTOFARM_MAX_ZONE_NODES = farm.getProperty("MAX_ZONE_NODES", 15);
		AUTOFARM_MAX_ROUTE_NODES = farm.getProperty("MAX_ROUTE_NODES", 30);
		AUTOFARM_MAX_TIMER = farm.getProperty("MAX_TIMER", 0);
		AUTOFARM_HP_HEAL_RATE = farm.getProperty("HP_HEAL_RATE", 80) / 100.;
		AUTOFARM_MP_HEAL_RATE = farm.getProperty("MP_HEAL_RATE", 80) / 100.;
		AUTOFARM_DEBUFF_CHANCE = farm.getProperty("DEBUFF_CHANCE", 30);
		AUTOFARM_HP_POTIONS = farm.getProperty("HP_POTIONS", new int[0]);
		AUTOFARM_MP_POTIONS = farm.getProperty("MP_POTIONS", new int[0]);
		AUTOFARM_ALLOW_DUALBOX = farm.getProperty("ALLOW_DUALBOX", true);
		AUTOFARM_DISABLE_TOWN = farm.getProperty("DISABLE_TOWN", true);
		AUTOFARM_SHOW_ROUTE_RANGE = farm.getProperty("SHOW_ROUTE_RANGE", true);
		AUTOFARM_SEND_LOG_MESSAGES = farm.getProperty("SEND_LOG_MESSAGES", false);
		AUTOFARM_CHANGE_PLAYER_TITLE = farm.getProperty("CHANGE_PLAYER_TITLE", false);
		AUTOFARM_CHANGE_PLAYER_NAME_COLOR = farm.getProperty("CHANGE_PLAYER_NAME_COLOR", false);
		AUTOFARM_NAME_COLOR = farm.getProperty("PLAYER_NAME_COLOR", "000000");
	}

	// Carrega os Comandos de Voz
	public static final void loadVoicedCommand()
	{
		final ExProperties VoincedCommand = initProperties(VoicedCommand_FILE);
		COMMAND_ONLINE = VoincedCommand.getProperty("CommandOnline", false);
		BANKING_SYSTEM = VoincedCommand.getProperty("BankingSystem", false);
		BANKING_SYSTEM_ITEM = VoincedCommand.parseIntIntList("BankingItemCount", "3470-1");
		BANKING_SYSTEM_ADENA = VoincedCommand.getProperty("BankingAdenaCount", 500000000);
	}

    // Carrega as Configurações VIP
	public static final void loadVipConfig(){

		final ExProperties vip = initProperties(VIP_FILE);

		NEW_CHAR_VIP = vip.getProperty("NEW_CHAR_VIP", false);
		DEFAULT_VIP_LEVEL = vip.getProperty("DEFAULT_VIP_LEVEL", 0);
		DEFAULT_VIP_EXP = vip.getProperty("DEFAULT_VIP_EXP", 0);
		DEFAULT_VIP_TIME = vip.getProperty("DEFAULT_VIP_TIME", 604800);
	}

	public static final void loadGameServer()
	{
		LOGGER.info("Carregando arquivos de configuração do servidor de jogo.");
		
		// configurações de clãs
		loadClans();
		
		// configurações de eventos
		loadEvents();
		
		// configurações do geoengine
		loadGeoengine();
		
		// hexID
		loadHexID();
		
		// configurações de NPCs/monstros
		loadNpcs();
		
		// configurações de jogadores
		loadPlayers();
		
		// configurações de siege
		loadSieges();
		
		// configurações do servidor
		loadServer();

		// configurações do Autofarm
		loadAutoFarm();

		//Carrega os Comandos de Voz
		loadVoicedCommand();

		//Carrega as Configurações VIP
		loadVipConfig();
	}
	
	public static final void loadLoginServer()
	{
		LOGGER.info("Carregando arquivos de configuração do servidor de login.");
		
		// configurações de login
		loadLogin();
	}
	
	public static final void loadAccountManager()
	{
		LOGGER.info("Carregando arquivos de configuração do gerenciador de contas.");
		
		// configurações de login
		loadLogin();
	}
	
	public static final void loadGameServerRegistration()
	{
		LOGGER.info("Carregando arquivos de configuração de registro do servidor de jogo.");
		
		// configurações de login
		loadLogin();
	}

	public static final void loadAutoFarm()
	{
		LOGGER.info("Carregando arquivos de configuração do auto farm.");

		// configurações de login
		loadAutoFarmManager();
	}

	public static final class ClassMasterSettings
	{
		private final Map<Integer, Boolean> _allowedClassChange;
		private final Map<Integer, List<IntIntHolder>> _claimItems;
		private final Map<Integer, List<IntIntHolder>> _rewardItems;
		
		private ClassMasterSettings(String configLine)
		{
			_allowedClassChange = HashMap.newHashMap(3);
			_claimItems = HashMap.newHashMap(3);
			_rewardItems = HashMap.newHashMap(3);
			
			if (configLine != null)
				parseConfigLine(configLine.trim());
		}
		
		private void parseConfigLine(String configLine)
		{
			StringTokenizer st = new StringTokenizer(configLine, ";");
			while (st.hasMoreTokens())
			{
				// Obtém a mudança de classe permitida.
				int job = Integer.parseInt(st.nextToken());
				
				_allowedClassChange.put(job, true);
				
				List<IntIntHolder> items = new ArrayList<>();
				
				// Analisa os itens necessários para a mudança de classe.
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						items.add(new IntIntHolder(Integer.parseInt(st3.nextToken()), Integer.parseInt(st3.nextToken())));
					}
				}
				
				// Alimenta o mapa e limpa a lista.
				_claimItems.put(job, items);
				items = new ArrayList<>();
				
				// Analisa os presentes após a mudança de classe.
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						items.add(new IntIntHolder(Integer.parseInt(st3.nextToken()), Integer.parseInt(st3.nextToken())));
					}
				}
				
				_rewardItems.put(job, items);
			}
		}
		
		public boolean isAllowed(int job)
		{
			if (_allowedClassChange == null)
				return false;
			
			if (_allowedClassChange.containsKey(job))
				return _allowedClassChange.get(job);
			
			return false;
		}
		
		public List<IntIntHolder> getRewardItems(int job)
		{
			return _rewardItems.get(job);
		}
		
		public List<IntIntHolder> getRequiredItems(int job)
		{
			return _claimItems.get(job);
		}
	}
}
