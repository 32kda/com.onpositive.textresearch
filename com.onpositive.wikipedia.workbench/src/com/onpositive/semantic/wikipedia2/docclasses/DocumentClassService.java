package com.onpositive.semantic.wikipedia2.docclasses;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSetSerializable;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.date.FreeFormDateParser;
import com.onpositive.semantic.search.core.date.FreeFormDateParser.FreeFormDateParserConfig;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;
import com.onpositive.semantic.wikipedia2.catrelations.ThemeIndex;
import com.onpositive.semantic.wikipedia2.docclasses.SimplePropertyStorage.PropertyVisitor;
import com.onpositive.semantic.wikipedia2.services.RedirectsMap;

public class DocumentClassService extends WikiEngineService {

	protected DocumentClasses data;

	public DocumentClassService(WikiEngine2 engine) {
		super(engine);
	}

	@Override
	protected void doLoad(File fl) throws IOException {
		ObjectInputStream stream = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(fl)));
		try {
			data = (DocumentClasses) stream.readObject();
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException();
		}
		stream.close();
	}

	static HashMap<String, String> propToCategory = new HashMap<String, String>();

	static {
		propToCategory.put("телефонный код", DocumentClasses.LOCATIONCLASS);
		propToCategory.put("население", DocumentClasses.LOCATIONCLASS);
		propToCategory.put("дата основания", DocumentClasses.LOCATIONCLASS);
		propToCategory.put("дата рождения", DocumentClasses.PERSONCLASS);
		propToCategory.put("место рождения", DocumentClasses.PERSONCLASS);
		propToCategory.put("дата смерти", DocumentClasses.PERSONCLASS);
		propToCategory.put("оборот", DocumentClasses.ORGANIZATIONCLASS);
		propToCategory.put("год переписи", DocumentClasses.LOCATIONCLASS);
		propToCategory.put("высота центра нп", DocumentClasses.LOCATIONCLASS);
		propToCategory
				.put("национальный состав", DocumentClasses.LOCATIONCLASS);
		propToCategory.put("конфессиональный состав",
				DocumentClasses.LOCATIONCLASS);
		propToCategory.put("почтовые индексы", DocumentClasses.LOCATIONCLASS);

	}

	@Override
	protected void build(WikiEngine2 enfine) {
		try {
			data = new DocumentClasses();
			SimplePropertyStorage.visitPropertiesFile(new PropertyVisitor() {

				@Override
				public void visit(int document, String pName, String value) {
					if (pName.equals("template")) {
						if (value.equals("Музыкальный коллектив")) {
							data.markClass(DocumentClasses.MUSICGROUPCLASS,
									document);
						}
						if (value.equals("Музыкальный сингл")) {
							data.markClass(DocumentClasses.SINGLECLASS,
									document);
						}
						if (value.equals("Фильм")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.equals("Музыкальный альбом")) {
							data.markClass(DocumentClasses.MUSICALBUMCLASS,
									document);
						}
						if (value.equals("Карточка игры")) {
							data.markClass(DocumentClasses.VIDEOGAMES, document);
						}
						if (value.equals("Серия игр")) {
							data.markClass(DocumentClasses.VIDEOGAMES, document);
						}
						if (value.startsWith("Судно/")) {
							data.markClass(DocumentClasses.SHIP, document);
						}
						if (value.equals("Карточка компании")) {
							data.markClass(DocumentClasses.ORGANIZATIONCLASS,
									document);
						}
						if (value.equals("Карточка программы")) {
							data.markClass(DocumentClasses.SOFTWAREENTITY,
									document);
						}
						if (value.equals("Таксон")) {
							data.markClass(DocumentClasses.SPIECESCLASS,
									document);
						}
						if (value.toLowerCase().equals("taxobox")) {
							data.markClass(DocumentClasses.SPIECESCLASS,
									document);
						}
						if (value.equals("Река")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.startsWith("Улица")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Залив")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Хребет")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Вулкан")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Автодорога")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Мост")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Железнодорожная станция")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Озеро")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.toLowerCase().contains("организация")) {
							data.markClass(DocumentClasses.ORGANIZATIONCLASS,
									document);
						}
						if (value.toLowerCase().contains("галактика")) {
							data.markClass(DocumentClasses.ASTROCLASS, document);
						}
						if (value.contains("Карточка ФК")) {
							data.markClass(DocumentClasses.SPORTCOMMAND,
									document);
						}
						if (value.contains("МатчВолейбол")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.contains("Произведение искусства")) {
							data.markClass(DocumentClasses.CULTUREENTITY,
									document);
						}
						if (value.contains("Мечеть")) {
							data.markClass(DocumentClasses.RELIGIOUSBUILDING,
									document);
						}
						if (value.contains("таксон")) {
							data.markClass(DocumentClasses.SPIECESCLASS,
									document);
						}
						if (value.contains("белок")) {
							data.markClass(DocumentClasses.SUBSTANCECLASS,
									document);
						}
						if (value.contains("Космический аппарат")) {
							data.markClass(DocumentClasses.SPACE_DEVICE,
									document);
						}
						if (value.contains("Хоккейный клуб")) {
							data.markClass(DocumentClasses.SPORTCOMMAND,
									document);
						}
						if (value.contains("Минерал")) {
							data.markClass(DocumentClasses.SUBSTANCECLASS,
									document);
						}
						if (value.contains("hockeybox2")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.contains("footballbox collapsible")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.contains("Матч водное поло")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.contains("footballbox")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.contains("Карточка цифрового фотоаппарата")) {
							data.markClass(DocumentClasses.TECHDEVICE, document);
						}
						if (value.contains("Карточка фотоаппарата")) {
							data.markClass(DocumentClasses.TECHDEVICE, document);
						}
						if (value.contains("Лекарственное средство")) {
							data.markClass(DocumentClasses.TREATMENT, document);
						}
						if (value.contains("Космический аппарат")) {
							data.markClass(DocumentClasses.SPACE_DEVICE,
									document);
						}
						if (value.contains("Авиакатастрофа")) {
							data.markClass(DocumentClasses.AIRCRAFT_CRASH,
									document);
						}
						if (value.contains("МатчВолейбол")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.contains("МатчБадминтон")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.contains("Паровоз")) {
							data.markClass(DocumentClasses.RAIL, document);
						}
						if (value.contains("Тепловоз")) {
							data.markClass(DocumentClasses.RAIL, document);
						}

						if (value.contains("Эпизод South Park")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.contains("Полуостров")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.contains("Карточка языка программирования")) {
							data.markClass(DocumentClasses.SOFTWAREENTITY,
									document);
						}
						if (value.contains("Infobox software")) {
							data.markClass(DocumentClasses.SOFTWAREENTITY,
									document);
						}
						if (value.contains("Турнир")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}

						if (value.contains("Памятник")) {
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
						}
						if (value.contains("карточка компании")) {
							data.markClass(DocumentClasses.ORGANIZATIONCLASS,
									document);
						}
						if (value.contains("Карточка института")) {
							data.markClass(DocumentClasses.ORGANIZATIONCLASS,
									document);
						}
						if (value.startsWith("Звёздное скопление")) {
							data.markClass(DocumentClasses.ASTROCLASS, document);
						}
						if (value.contains("Телеканал")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.contains("Аэропорт данные")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Карточка сезона командного турнира")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.equals("МатчХоккей")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.equals("Болезнь")) {
							data.markClass(DocumentClasses.DECEASECLASS,
									document);
						}
						if (value.equals("Infobox Weapon")) {
							data.markClass(DocumentClasses.WEAPON, document);
						}
						if (value.equals("Мультсериал")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.equals("Водохранилище")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Карточка ОС")) {
							data.markClass(DocumentClasses.SOFTWAREENTITY,
									document);
						}
						if (value.equals("Сотовый телефон")) {
							data.markClass(DocumentClasses.TECHDEVICE, document);
						}
						if (value.equals("Planetbox discovery")) {
							data.markClass(DocumentClasses.ASTROCLASS, document);
						}
						if (value.equals("Infobox Military Conflict")) {
							data.markClass(DocumentClasses.MILITARY_CONFLICT,
									document);
						}
						if (value.equals("Planetbox star")) {
							data.markClass(DocumentClasses.ASTROCLASS, document);
						}
						if (value.equals("Planetbox begin")) {
							data.markClass(DocumentClasses.ASTROCLASS, document);
						}
						if (value.equals("Planetbox orbit")) {
							data.markClass(DocumentClasses.ASTROCLASS, document);
						}
						if (value.equals("карточка программы")) {
							data.markClass(DocumentClasses.SOFTWAREENTITY,
									document);
						}
						if (value.equals("Компьютерная игра")) {
							data.markClass(DocumentClasses.VIDEOGAMES, document);
						}
						if (value.equals("Танк2")) {
							data.markClass(DocumentClasses.TANK, document);
						}

						if (value.equals("Карточка оружия")) {
							data.markClass(DocumentClasses.WEAPON, document);
						}
						if (value.equals("Карточка института")) {
							data.markClass(DocumentClasses.ORGANIZATIONCLASS,
									document);
						}
						if (value.equals("карточка театра")) {
							data.markClass(DocumentClasses.ORGANIZATIONCLASS,
									document);
							data.markClass(DocumentClasses.CULTUREENTITY,
									document);
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
						}
						if (value.equals("Гоночный автомобиль")) {
							data.markClass(DocumentClasses.AUTO, document);
						}
						if (value.equals("Book")) {
							data.markClass(DocumentClasses.BOOKCLASS, document);
						}
						if (value.equals("Аэропорт")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Песня")) {
							data.markClass(DocumentClasses.SINGLECLASS,
									document);
						}
						if (value.equals("Телепередача")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.equals("произведение искусства")) {
							data.markClass(DocumentClasses.CULTUREENTITY,
									document);
						}
						if (value.equals("музей")) {
							data.markClass(DocumentClasses.CULTUREENTITY,
									document);
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
						}
						if (value.equals("Танк")) {
							data.markClass(DocumentClasses.TANK, document);
						}
						if (value.contains("Музыкальный альбом")) {
							data.markClass(DocumentClasses.MUSICALBUMCLASS,
									document);
						}
						if (value.equals("Компьютерная игра")) {
							data.markClass(DocumentClasses.VIDEOGAMES, document);
						}
						if (value.equals("Танк2")) {
							data.markClass(DocumentClasses.TANK, document);
						}
						if (value.equals("Карточка банка")) {
							data.markClass(DocumentClasses.ORGANIZATIONCLASS,
									document);
						}
						if (value.startsWith("Национальный чемпионат")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.equals("Турнир16-Теннис3")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.startsWith("Воинское формирование")) {
							data.markClass(DocumentClasses.MILITARY_UNIT,
									document);
						}
						if (value.equals("Воинское формирование")) {
							data.markClass(DocumentClasses.MILITARY_UNIT,
									document);
						}
						if (value.equals("карточка театра")) {
							data.markClass(DocumentClasses.ORGANIZATIONCLASS,
									document);
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
							data.markClass(DocumentClasses.CULTUREENTITY,
									document);
						}
						if (value.equals("Парк")) {
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}

						if (value.toLowerCase().contains("Episode list")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.toLowerCase().contains("вещество")) {
							data.markClass(DocumentClasses.SUBSTANCECLASS,
									document);
						}
						if (value.equals("Мультфильм")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.equals("Телесериал")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.equals("Вооружённый конфликт")) {
							data.markClass(DocumentClasses.MILITARY_CONFLICT,
									document);
						}
						if (value.equals("Литературное произведение")) {
							data.markClass(DocumentClasses.BOOKCLASS, document);
						}
						if (value.toLowerCase().equals("книга")) {
							data.markClass(DocumentClasses.BOOKCLASS, document);
						}
						if (value.toLowerCase().equals(
								"footballbox_collapsible")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.equals("Hockeybox2")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}

						if (value.toLowerCase().equals("japanese episode list")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.toLowerCase().equals("отчёт о матче")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.toLowerCase().equals("матчбаскетбол")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.toLowerCase().equals(
								"карточка футбольного матча")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.toLowerCase().equals("матчгандбол")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.equals("Малая планета")) {
							data.markClass(DocumentClasses.ASTROCLASS, document);
						}
						if (value.equals("Достопримечательность")) {
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
						}
						if (value.equals("Список серий аниме")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.equals("Военное подразделение")) {
							data.markClass(DocumentClasses.MILITARY_UNIT,
									document);
						}
						if (value.equals("Автомобиль")) {
							data.markClass(DocumentClasses.AUTO, document);
						}
						if (value.equals("Карточка ЛА")) {
							data.markClass(DocumentClasses.AIRSHIP, document);
						}
						if (value.equals("Подводная лодка")) {
							data.markClass(DocumentClasses.SHIP, document);
						}
						if (value.equals("Сингл")) {
							data.markClass(DocumentClasses.SINGLECLASS,
									document);
						}
						if (value.equals("Музей")) {
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
						}
						if (value.equals("Список серий")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.equals("Звезда")) {
							data.markClass(DocumentClasses.ASTROCLASS, document);
						}
						if (value.equals("Турнир16-Теннис3-bye")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.equals("Турнир16-Теннис3-bye")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.equals("Газета")) {
							data.markClass(DocumentClasses.PRESS, document);
						}
						if (value.equals("Журнал")) {
							data.markClass(DocumentClasses.PRESS, document);
						}
						if (value.equals("Издание")) {
							data.markClass(DocumentClasses.PRESS, document);
						}
						if (value.equals("Вершина")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Episode list")) {
							data.markClass(DocumentClasses.FILMCLASS, document);
						}
						if (value.equals("Карточка университета")) {
							data.markClass(DocumentClasses.LISTCLASS, document);
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
						}
						if (value.equals("Храм")) {
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
							data.markClass(DocumentClasses.RELIGIOUSBUILDING,
									document);
						}
						if (value.equals("Монастырь")) {
							data.markClass(DocumentClasses.SIGHTSEEING,
									document);
							data.markClass(DocumentClasses.RELIGIOUSBUILDING,
									document);
						}
						if (value.equals("Гран-при Формулы-1")) {
							data.markClass(DocumentClasses.MATCHCLASS, document);
						}
						if (value.equals("Станция метро")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
						if (value.equals("Стадион")) {
							data.markClass(DocumentClasses.LOCATIONCLASS,
									document);
						}
					} else {
						
						String lowerCase = propToCategory.get(pName
								.toLowerCase());
						if (lowerCase != null) {
							data.markClass(lowerCase, document);
						}
						
					}
				}

			}, enfine);
			RedirectsMap index = enfine.getIndex(RedirectsMap.class);
			for (int q : enfine.getDocumentIDs()) {
				String string = engine.getPageTitles().get(q);
				
				if (string.toLowerCase().startsWith("список")) {
					data.markClass(DocumentClasses.LISTCLASS, q);
				}
				if (index.isRedirect(q)) {
					data.markClass(DocumentClasses.REDIRECTCLASS, q);
				}
				else{
					ICategory[] categories = new WikiDoc(engine, q).getCategories();
					for (ICategory c:categories){
						if (c.getTitle().toLowerCase().contains("родившиеся")){
							data.markClass(DocumentClasses.PERSONCLASS, q);
						}
						if (c.getTitle().toLowerCase().contains("персоналии")){
							data.markClass(DocumentClasses.PERSONCLASS, q);
						}
						if (c.getTitle().toLowerCase().contains("_год_в_")){
							data.markClass(DocumentClasses.EVENTCLASS, q);
						}
						if (c.getTitle().toLowerCase().contains("cобытия")){
							data.markClass(DocumentClasses.EVENTCLASS, q);
						}
						if (c.getTitle().contains("Википедия:")){
							continue;
						}
						if (c.getTitle().contains("после")){
							continue;
						}
						if (c.getTitle().contains("до")){
							continue;
						}
						List<IFreeFormDate> parse2 = FreeFormDateParser.parse(
								c.getTitle(), new FreeFormDateParserConfig(), new ArrayList<Integer>());
						if (!parse2.isEmpty()){
							data.markClass(DocumentClasses.EVENTCLASS, q);							
						}
					}
				}
			}			
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		System.out.println(data.classified.size() + ":"
				+ enfine.getDocumentIDs().length);
		for (String s : data.documentsByClass.keySet()) {
			System.out.println(s + ":" + data.documentsByClass.get(s).size());
		}
	}

	@Override
	protected void doSave(File fl) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(fl)));
		objectOutputStream.writeObject(data);
		objectOutputStream.close();
	}

	@Override
	public String getFileName() {
		return "docClassesDat";
	}

	public boolean isClassified(int document) {
		return data.classified.contains(document);
	}

	public int getClassified() {
		return data.classified.size();
	}

	public Set<String> getDocumentClasses() {
		return data.documentsByClass.keySet();
	}

	public int getClassSize(String s) {
		return data.documentsByClass.get(s).size();
	}

	public boolean hasClass(int d, String metaClasss) {
		String[] strings = DocumentClasses.metaClassMappings.get(metaClasss);
		if (strings != null) {
			for (String s : strings) {
				HashMap<String, IntOpenHashSetSerializable> documentsByClass = data.documentsByClass;
				IntOpenHashSetSerializable intOpenHashSetSerializable = documentsByClass.get(s);
				if (documentsByClass!=null&&intOpenHashSetSerializable!=null&&intOpenHashSetSerializable.contains(d)) {
					return true;
				}
			}
		}
		if (data.documentsByClass.containsKey(metaClasss)) {
			return data.documentsByClass.get(metaClasss).contains(d);
		}
		return false;
	}
}
