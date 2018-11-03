package com.vitaviva.commongallery.data;

import android.text.TextUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class DataManger {

    private static DataManger sInstance;


    private DataManger() {

    }

    public static DataManger getInstance() {
        if (sInstance == null) {
            sInstance = new DataManger();
        }
        return sInstance;
    }

    private static String[] datas = {
            "https://ss0.baidu.com/-Po3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/c2cec3fdfc0392452d1704a98594a4c27c1e25f2.jpg",
            "", "", "", "", "",//模拟空洞
            "https://ss0.bdstatic.com/94oJfD_bAAcT8t7mm9GUKT-xh_/timg?image&quality=100&size=b4000_4000&sec=1540973081&di=2675b8ec4b2271b7f8c945bf7dd1e0a8&src=http://abc.2008php.com/2014_Website_appreciate/2014-10-12/20141012115151.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1541146116&di=0d7c684ce5061e874d930538bcbd64eb&src=http://photocdn.sohu.com/20070620/Img250684019.jpg",
            "", "",//模拟空洞
            "https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1541146179&di=c505b3059ed4bc17a30b93dfd2a9654a&src=http://img3.duitang.com/uploads/blog/201401/09/20140109091949_sCjhv.thumb.700_0.jpeg",
            "https://ss0.baidu.com/7Po3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/0eb30f2442a7d933878346a8ae4bd11372f001c5.jpg",
            "",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1541146179&di=c67aaf05aec9eb1ef6c6c25a971c9b3d&src=http://o.neeu.com/uploads/photo2/644/644534/1418192345188.jpg"
    };

    public int getDataCount() {
        return Integer.MAX_VALUE;
    }

    public String getData(int index) {
        if (index < 0) return null;
        return datas[index % datas.length];
    }

    public List<Integer> getPreData(int index, int count) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            if (index - i >= 0)
                list.add(index - i);
        }
        return list;
    }

    public List<Integer> getNextData(int index, int count) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            if (index + i >= 0)
                list.add(index + i);
        }
        return list;
    }

    public boolean isValid(int index) {
        return !TextUtils.isEmpty(getData(index));
    }
}
