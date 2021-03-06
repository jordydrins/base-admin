package cn.huanzi.qch.baseadmin.user.service;

import cn.huanzi.qch.baseadmin.common.pojo.PageInfo;
import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.common.service.CommonServiceImpl;
import cn.huanzi.qch.baseadmin.sys.sysuser.repository.ProductSuRepository;
import cn.huanzi.qch.baseadmin.sys.sysuser.vo.ProductSuVo;
import cn.huanzi.qch.baseadmin.user.model.ProductSu;
import cn.huanzi.qch.baseadmin.util.CopyUtil;
import cn.huanzi.qch.baseadmin.util.SqlUtil;
import org.hibernate.query.internal.NativeQueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author yebin
 * @version 1.0
 * @className LiOrderServiceImpl
 * @description TODO
 * @date 2020/8/11 0:28
 **/
@Service
public class ProductSuServiceImpl extends CommonServiceImpl<ProductSuVo, ProductSu, Long> implements ProductSuService {

    @Autowired
    private ProductSuRepository productSuRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Result<PageInfo<ProductSuVo>> page(ProductSuVo entityVo) {
        //SQL
        ProductSu entity = CopyUtil.copy(entityVo, ProductSu.class);
        StringBuilder sql = SqlUtil.appendFields(entity);
        SqlUtil.appendQueryColumns(entity, sql, "queryOrderStartTime", "queryOrderEndTime");

        //设置SQL、映射实体，以及设置值，返回一个Query对象
        Query query = em.createNativeQuery(sql.toString(), ProductSu.class);

        //分页、排序信息，并设置，page从0开始
        PageRequest pageRequest = PageRequest.of(entityVo.getPage() - 1, entityVo.getRows(), new Sort(Sort.Direction.ASC, "id"));
        query.setFirstResult((int) pageRequest.getOffset());
        query.setMaxResults(pageRequest.getPageSize());

        //获取分页结果
        Page page = PageableExecutionUtils.getPage(query.getResultList(), pageRequest, () -> {
            //设置countQuerySQL语句
            Query countQuery = em.createNativeQuery("select count(1) from ( " + ((NativeQueryImpl) query).getQueryString() + " ) count_table");
            //设置countQuerySQL参数
            query.getParameters().forEach(parameter -> countQuery.setParameter(parameter.getName(), query.getParameterValue(parameter.getName())));
            //返回一个总数
            return Long.valueOf(countQuery.getResultList().get(0).toString());
        });

        Result<PageInfo<ProductSuVo>> result = Result.of(PageInfo.of(page, ProductSuVo.class));

        return result;
    }
}
