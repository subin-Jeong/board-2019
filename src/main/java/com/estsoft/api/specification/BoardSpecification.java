package com.estsoft.api.specification;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.estsoft.api.domain.Board;
import com.estsoft.util.ApiUtils;

/**
 * Board Repository �� ����
 * @author JSB
 */
public class BoardSpecification {
	
	/**
	 * �� ���
	 * @param searchParams
	 * @return Specification<Board>
	 */
	public static Specification<Board> list(final Map<String, String[]> searchParams) {
		
		return new Specification<Board>() {

			@Override
			public Predicate toPredicate(Root<Board> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				if(searchParams.isEmpty()) {
					return null;
				}
				
				// �˻� ���� �Ķ����
				String searchType = searchParams.get("searchType")[0];
				String searchString = searchParams.get("searchString")[0];
				String searchStartDate = searchParams.get("searchStartDate")[0];
				String searchEndDate = searchParams.get("searchEndDate")[0];
				
				List<Predicate> predicates = new ArrayList<>();
				
				// delFlag = 'N'
				// �������� ���� ��ϸ� ��ȸ
				predicates.add(cb.and(cb.equal(root.get("delFlag"), "N")));
				
				// �˻� �÷��� ���� �˻������ ����
				if(ApiUtils.isNotNullString(searchType) && (ApiUtils.isNotNullString(searchString) || ApiUtils.isNotNullString(searchStartDate) || ApiUtils.isNotNullString(searchEndDate))) {
					
					switch(searchType) {
					
						// ������ ��� LIKE ��
						case "title" :
							predicates.add(cb.like(root.get(searchType), "%" + searchString + "%"));
							break;
							
						// ������ ��� LIKE ��
						case "content" :
							predicates.add(cb.like(root.get(searchType), "%" + searchString + "%"));
							break;
							
						// ��¥ ���� ��
						case "regDate" :

							try {
								
								DateFormat df = new SimpleDateFormat("yyyy.MM.ddHH:mm:ss");

								if(ApiUtils.isNotNullString(searchStartDate)) {
									predicates.add(cb.greaterThanOrEqualTo(root.get(searchType), df.parse(searchStartDate + "00:00:00")));
								}
								
								if(ApiUtils.isNotNullString(searchEndDate)) {
									predicates.add(cb.lessThanOrEqualTo(root.get(searchType), df.parse(searchEndDate + "23:59:59")));
								}
								
							} catch (ParseException e) {
								e.printStackTrace();
							}
							
							break;
						
						default :
							
							// ������ �˻��� ����
							if(searchType.equals("no") && !ApiUtils.isStringInteger(searchString)) {
								searchString = "0";
							}
							
							predicates.add(cb.and(cb.equal(root.get(searchType), searchString)));
							break;
					}
				}
				return cb.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
	}

}
