package com.onpositive.semantic.wikipedia2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.IDocument;
import com.onpositive.semantic.search.core.ILocation;
import com.onpositive.semantic.search.core.MatchDescription;
import com.onpositive.semantic.search.core.SearchRequest;
import com.onpositive.semantic.search.core.date.FreeFormDateParser;
import com.onpositive.semantic.search.core.date.IFreeFormDate;
import com.onpositive.semantic.wikipedia2.fulltext.StemProvider;
import com.onpositive.semantic.wikipedia2.internal.RichTextsAbstractIndex;
import com.onpositive.semantic.wikipedia2.locations.DocumentGeoIndex.DocumentGeoMatch;
import com.onpositive.semantic.wikipedia2.locations.GeoIndex;
import com.onpositive.semantic.wikipedia2.locations.GeoSearchService;
import com.onpositive.semantic.wikipedia2.popularity.ArticlePopularityService;
import com.onpositive.semantic.wikipedia2.services.PageImagesIndex;

public class WikiDoc  implements IDocument{

	protected final WikiEngine2 engine;
	protected final int id;
	protected String title;
	static MessageDigest instance;
	private String[] images;

	public WikiDoc(WikiEngine2 engine, int id) {
		super();
		this.engine = engine;
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((engine == null) ? 0 : engine.hashCode());
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WikiDoc other = (WikiDoc) obj;
		if (engine == null) {
			if (other.engine != null)
				return false;
		} else if (!engine.equals(other.engine))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public String getId() {
		return engine.id() + '/' + id;
	}

	@Override
	public String getUrl() {
		return engine.getUrl() + "wiki/" + getTitle() != null ? getTitle()
				.replace(' ', '_') : "";
	}

	@Override
	public String getTitle() {
		if (title != null) {
			return title;
		}
		return engine.getPageTitles().get(id);
	}

	@Override
	public String getRichTextAbstract() {
		return RichTextsAbstractIndex.AbstractIndexProvider.getInstance()
				.get(engine).getAbstract(id);
	}

	@Override
	public String getPlainTextAbstract() {
		return engine.getTextAbstract(id);
	}

	@Override
	public String getOriginalMarkup() {
		return engine.getPlainTextAccess().getPage(id);
	}

	@Override
	public String getImageLink() {
		String[] im=getImages();
		if (im!=null&&im.length>0){
			return im[0];
		}
		return null;
	}

	@Override
	public ICategory[] getCategories() {
		int[] direct = engine.getPageToParentCategories().getInverse(id);
		ICategory[] c = new ICategory[direct.length];
		int a = 0;
		for (int q : direct) {
			c[a++] = new WikiCat(q, engine);
		}
		return c;
	}

	@Override
	public ArrayList<ILocation> getLocations() {
		List<DocumentGeoMatch> matches = engine.getIndex(GeoSearchService.class).getMatches(id);
		HashSet<String>str=new HashSet<String>();
		for (DocumentGeoMatch g:matches){
			int geoId = g.getGeoId();
			Collection<String> namesById = GeoIndex.getIndex().getNamesById(geoId);
			str.addAll(namesById);
		}
		ArrayList<ILocation>result=new ArrayList<ILocation>();
		for (final String s:str){
			result.add(new ILocation() {
				
				@Override
				public String getTitle() {
					return s;
				}
				
				@Override
				public String getId() {
					return s;
				}
			});
		}
		return result;
	}
	@Override
	public ArrayList<String> getDates() {
		ArrayList<String> result = new ArrayList<String>();
		ICategory[] documentCategories = getCategories();
		if (documentCategories != null) {
			for (ICategory cat : documentCategories) {
				List<IFreeFormDate> parsedDates = FreeFormDateParser.parse(cat
						.getTitle());
				for (IFreeFormDate parsedDate : parsedDates) {
					result.add(parsedDate.toString());
				}
			}
		}
		return result;
	}

	@Override
	public boolean hasCategory(ICategory c) {
		int[] inverse = engine.getPageToParentCategories().getInverse(id);
		if (c instanceof WikiCat) {
			WikiCat m = (WikiCat) c;
			for (int q : inverse) {
				if (q == m.id) {
					return true;
				}
			}
		}
		return false;
	}

	static long[] freeFormDateToGuaranteedRange(IFreeFormDate date) {
		long rangeStart;
		long rangeEnd;
		if (date != null) {
			Date startDate = date.getStartDate();
			Date endDate = date.getEndDate();
			if (startDate != null) {
				rangeStart = startDate.getTime();
			} else {
				rangeStart = 0;
			}

			if (endDate != null) {
				rangeEnd = endDate.getTime();
			} else {
				rangeEnd = System.currentTimeMillis();
			}
		} else {
			rangeStart = 0;
			rangeEnd = System.currentTimeMillis();
		}

		return new long[] { rangeStart, rangeEnd };
	}

	@Override
	public MatchDescription[] getMatches(SearchRequest request) {
		if (request == null) {
			return new MatchDescription[] {};
		}
		String location = request.getLocation() != null ? request.getLocation()
				.toLowerCase() : null;
		ArrayList<MatchDescription> matches = new ArrayList<MatchDescription>();
		if (location != null && location.length() > 0) {
			GeoSearchService index = engine.getIndex(GeoSearchService.class);
			List<DocumentGeoMatch> mt = index.getMatches(this.id, location);
			for (DocumentGeoMatch g : mt) {
				int position = g.getPosition();
				matches.add(new MatchDescription(MatchDescription.LOCATION,
						position, -1));
			}
		}
		String date = request.getDate();
		if (date != null && date.trim().length() > 0) {
			List<IFreeFormDate> parse = FreeFormDateParser.parse(date);
			if (parse.size() > 0) {
				IFreeFormDate iFreeFormDate = parse.get(0);
				long[] freeFormDateToGuaranteedRange = freeFormDateToGuaranteedRange(iFreeFormDate);
				long r0 = freeFormDateToGuaranteedRange[0];
				long r1 = freeFormDateToGuaranteedRange[1];
				String fullAbstract = getPlainTextAbstract();
				List<Integer> positions = new ArrayList<Integer>();
				List<IFreeFormDate> dates = FreeFormDateParser.parse(
						fullAbstract, positions);
				for (int a = 0; a < dates.size(); a++) {
					IFreeFormDate iFreeFormDate2 = dates.get(a);
					Date startDate = iFreeFormDate2.getStartDate();
					Date endDate = iFreeFormDate2.getEndDate();
					if (startDate != null && endDate != null) {
						long st = startDate.getTime();

						long et = endDate.getTime();
						if (st >= r0 && et <= r1) {
							Integer fromIndex = positions.get(a);
							int q=fullAbstract.indexOf('г', fromIndex);
							if (q!=-1&&q-fromIndex<50){
							matches.add(new MatchDescription(2, fromIndex, q-fromIndex));
							}
							else{
								matches.add(new MatchDescription(2, fromIndex-2, 10));
							}
						}
					}
				}
				// engine.getDateIndex().findMatchingDates(id, r0, r1, true,
				// null);
				/*
				 * for (int q:findMatchingDates.matchIndexes.toArray()){
				 * ds.add(new MatchDescription(1,
				 * findMatchingDates.positions.get(q), 10)); }
				 */
			}
		}
		if (request.getKeyword() != null) {
			String fullAbstract = getPlainTextAbstract();
			String lowerCase0 = request.getKeyword().toLowerCase();
			String[] split = lowerCase0.split(",");
			String[] stem = new String[split.length];
			int i = 0;
			for (String s : split) {
				stem[i++] = StemProvider.getInstance().stem(s.trim())
						.toLowerCase();
			}

			StringBuilder bld = new StringBuilder();

			for (int a = 0; a < fullAbstract.length(); a++) {
				char c = fullAbstract.charAt(a);
				if (Character.isJavaIdentifierPart(c)) {
					bld.append(c);
					continue;
				} else {
					if (bld.length() > 0) {
						String s = bld.toString();
						String lowerCase = StemProvider.getInstance()
								.stem(s.toLowerCase()).toLowerCase();
						for (String q : stem) {
							if (lowerCase.equals(q)) {
								matches.add(new MatchDescription(2, a
										- s.length(), s.length()));
							}
						}
						bld = new StringBuilder();
					} else {
						bld = new StringBuilder();
					}
				}
			}
		}
		return matches.toArray(new MatchDescription[matches.size()]);
	}

	static {
		try {
			instance = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	static String getImageLink(String oritinal) {
		try {
			byte[] input = oritinal.replace(' ', '_').getBytes("UTF-8");
			// $filename = replace($name, ' ', '_');
			// $digest = md5($filename);
			// $folder = $digest[0] . '/' . $digest[0] . $digest[1] . '/' .
			// urlencode($filename);
			// $url = 'http://upload.wikimedia.org/wikipedia/commons/' .
			// $folder;

			byte[] digest = instance.digest(input);
			String ssm = digestA(digest);
			return "http://upload.wikimedia.org/wikipedia/commons/"
					+ ssm.charAt(0) + '/' + ssm.charAt(0) + ssm.charAt(1) + '/'
					+ oritinal.replace(' ', '_');
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}

	private static String digestA(byte[] digest) {
		StringBuilder bld = new StringBuilder();
		for (byte q : digest) {
			bld.append(conc(q));
		}
		return bld.toString();
	}

	protected static String conc(byte b) {
		int q = b;
		String hexString2 = Integer.toHexString(q);
		if (hexString2.length() == 1) {
			hexString2 = '0' + hexString2;
		}
		String hexString = hexString2.substring(hexString2.length() - 2,
				hexString2.length());
		return hexString;
	}

	@Override
	public String[] getImages() {
		if (images!=null){
			return images;
		}
		images = engine.getIndex(PageImagesIndex.class).getImages(id);
		String[] result = new String[images.length];
		for (int a = 0; a < result.length; a++) {
			result[a] = getImageLink(images[a]);
		}
		return result;
	}

	@Override
	public int getPopularity() {
		return engine.getIndex(ArticlePopularityService.class).getTotalPopularity(id);
	}

	@Override
	public void adjustPopularity(int i) {
		engine.getIndex(ArticlePopularityService.class).vote(id, i);
		engine.clearPop();
	}

	public int getIntId() {
		return id;
	}

	public final WikiEngine2 getEngine() {
		return engine;
	}

}
