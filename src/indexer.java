
import com.mongodb.*;


import java.lang.reflect.Array;
import java.util.*;

public class indexer {
    private Map <String,PriorityQueue<word>> data;
    private Map <String,ArrayList<Integer>> urls;
    private DBCollection words_collection;
    public indexer(List<DBObject> pages,DBCollection words_collection)
    {
        this.data=new HashMap();
        this.urls=new HashMap();
        this.words_collection=words_collection;

        for(DBObject page:pages)
        {
            ArrayList<Integer>temp=new ArrayList<Integer>();
            temp.add(Integer.parseInt(page.get("popularity").toString()));
            temp.add(Integer.parseInt(page.get("video").toString()));
            this.urls.put(page.get("url").toString(), temp);
        }

    }

    public void add_word(String w,String page,int position,int rank,String origin,int size)
    {
        PriorityQueue<word> new_row;
        if(this.data.get(w)!=null)
            new_row=this.data.get(w);
        else
            new_row=new PriorityQueue();

        new_row.add(new word(page,position,rank,origin,size));
        this.data.put(w, new_row);
    }

    public Map<String, PriorityQueue<word>> getData() {
        return data;
    }


    public Map<String,Integer> search( String query,int img,int video,boolean phrase)
    {
        query=query.trim().replaceAll(" +", " ");
        List<String> words=new ArrayList();
        String[] splitArray=query.split(" ");

        for (String single_word : splitArray)
            words.add(single_word);

        for(int i=0;words.size()>i;++i)
        {

            DBCursor cursor = this.words_collection.find(new BasicDBObject("word",splitArray[i]));
            List<DBObject> words_data = cursor.toArray();
            for (DBObject object : words_data)
            {
                BasicDBList list =(BasicDBList) object.get("list");
                for(Object obj:list)
                {
                    BasicDBObject x=(BasicDBObject)obj;
                    this.add_word(words.get(i),x.get("url").toString(),Integer.parseInt(x.get("position").toString()),Integer.parseInt(x.get("rank").toString()),x.get("origin").toString(),Integer.parseInt(x.get("size").toString()));

                }

            }
        }
        String stemmed_query=Stemmer.stem(query);
        //      System.out.println(stemmed_query);
        if(phrase)
            return this.phrase_ranker(query,query,img,video);
        else
            return this.ranker(query,query,img,video);

    }

    public Map<String,Integer> ranker(String query,String origin_query,int img,int video)
    {
        //String stem_query=fun(query);
        System.out.println("here");
        String[] splitArray = query.trim().replaceAll(" +", " ").toLowerCase().split(" ");
        String[] originArray = origin_query.trim().replaceAll(" +", " ").toLowerCase().split(" ");

        ArrayList<String>original_query=new ArrayList<String>();

        //convert queues into 2d array list
        ArrayList<ArrayList<word>> lists= new ArrayList();
        Map<String ,Page> Pages=new HashMap();
        int index=-1;
        for (String single_word : splitArray)
            if(data.get(single_word)!=null)
                if(data.get(single_word).size()>0)
                {
                    lists.add(new ArrayList(data.get(single_word)));
                    original_query.add(originArray[++index]);
                }

        //initialize idfs and tfs
        int listSize=lists.size();
        ArrayList<Double> idfs=new ArrayList<Double>();
        ArrayList<Integer> tfs=new ArrayList<Integer>();
        int sizes=0;
        for(int i=0;i<listSize;i++)
        {
            tfs.add(1);
            sizes+=lists.get(i).size();
        }

        for(int i=0;i<listSize;i++) idfs.add((1.0*(sizes-lists.get(i).size()))/sizes);

        while (lists.size()>0)
        {
            //initialize firs word rank
            int first_position,position=lists.get(0).get(0).position;
            first_position=position;
            String current_page=lists.get(0).get(0).page;
            double pre_rank=lists.get(0).get(0).rank*tfs.get(0)*idfs.get(0)*((lists.get(0).get(0).origin.equals(original_query.get(0)))? 2 : 1);
            if(tfs.get(0)>=(lists.get(0).get(0).page_size)/3)pre_rank*=-1;
            int image=(lists.get(0).get(0).rank==0)? 1:0;
            tfs.set(0,tfs.get(0));
            lists.get(0).remove(0);
            if(lists.get(0).size()==0)
            {
                lists.remove(0);
                tfs.remove(0);
                idfs.remove(0);
                original_query.remove(0);
            }
            //loop 3alehom
            for(int j=1;j<lists.size();++j)
            {
                for(int k=0;k<lists.get(j).size();++k)
                {
                    if(lists.get(j).get(k).page.equals(current_page))
                    {
                        if (lists.get(j).get(k).position > position)
                        {
                            word temp = lists.get(j).get(k);
                            int check = 1, originality = 1;
                            if (tfs.get(j) >= (temp.page_size) / 3) check = -1;
                            double word_rank = temp.rank * tfs.get(j) * idfs.get(j) * ((temp.origin.equals(original_query.get(j))) ? 2 : 1) * check;
                            if(Pages.get(current_page)==null)
                                Pages.put(current_page,new Page(current_page, (word_rank + pre_rank) / (position - temp.position), (temp.rank==0||image==1) ? 1 : 0,position));
                            else
                            {
                                Pages.get(current_page).rank+=(word_rank + pre_rank) / (position - temp.position);
                                if(Pages.get(current_page).img==0)
                                    Pages.get(current_page).img=(temp.rank==0||image==1) ? 1 : 0;
                            }
                            position = temp.position;
                            lists.get(j).remove(k);
                            if (lists.get(j).size() == 0) {
                                lists.remove(j);
                                tfs.remove(j);
                                idfs.remove(j);
                                original_query.remove(j);
                                --j;
                            }
                            break;
                        }
                    }
                    else break;
                }
            }
            if(position==first_position)
                if(Pages.get(current_page)==null) Pages.put(current_page,new Page(current_page,pre_rank,image,position));
                else
                {
                    Pages.get(current_page).rank+=pre_rank;
                    if(Pages.get(current_page).img==0)
                        Pages.get(current_page).img=1;
                }

        }
        PriorityQueue<Page> queue=new PriorityQueue<Page>();
        for (Map.Entry<String, Page> entry : Pages.entrySet())
        {
            if((video==1&&this.urls.get(entry.getKey()).get(1)==1)||(img==1&&entry.getValue().img==1)||(img==0&&video==0))
            {
                entry.getValue().rank*=this.urls.get(entry.getKey()).get(0);

                queue.add(entry.getValue());
            }
        }

Map<String,Integer>output=new HashMap<>();
        for(Page anObject : queue)
        {

            output.put(anObject.page,anObject.positian);
        }
        return output;
    }

    public Map<String,Integer> phrase_ranker(String query,String origin_query,int img,int video)
    {
        boolean found=true;
        //String stem_query=fun(query);
        String[] splitArray = query.trim().replaceAll(" +", " ").toLowerCase().split(" ");
        String[] originArray = origin_query.trim().replaceAll(" +", " ").toLowerCase().split(" ");

        ArrayList<String>original_query=new ArrayList<String>();

        //convert queues into 2d array list
        ArrayList<ArrayList<word>> lists= new ArrayList();
        Map<String ,Page> Pages=new HashMap();
        Map<String ,Page> Pages2=new HashMap();
        int index=-1;
        for (String single_word : splitArray)
        {
            if(data.get(single_word)!=null)
            {
                if(data.get(single_word).size()>0)
                {
                    lists.add(new ArrayList(data.get(single_word)));
                    original_query.add(originArray[++index]);
                }
            }
            else return new HashMap<>();
        }


        //initialize idfs and tfs
        int listSize=lists.size();
        ArrayList<Double> idfs=new ArrayList<Double>();
        ArrayList<Integer> tfs=new ArrayList<Integer>();
        int sizes=0;
        for(int i=0;i<listSize;i++)
        {
            tfs.add(1);
            sizes+=lists.get(i).size();
        }

        for(int i=0;i<listSize;i++) idfs.add((1.0*(sizes-lists.get(i).size()))/sizes);

        while (lists.size()>0)
        {
            //initialize firs word rank
            int first_position,position=lists.get(0).get(0).position;
            first_position=position;
            String current_page=lists.get(0).get(0).page;
            double pre_rank=lists.get(0).get(0).rank*tfs.get(0)*idfs.get(0)*((lists.get(0).get(0).origin.equals(original_query.get(0)))? 2 : 1);
            if(tfs.get(0)>=(lists.get(0).get(0).page_size)/3)pre_rank*=-1;
            int image=(lists.get(0).get(0).rank==0)? 1:0;
            tfs.set(0,tfs.get(0));
            lists.get(0).remove(0);
            if(lists.get(0).size()==0)
            {
                lists.remove(0);
                tfs.remove(0);
                idfs.remove(0);
                found=false;
            }
            //loop 3alehom
            for(int j=1;j<lists.size();++j)
            {
                if(!found)break;
                for(int k=0;k<lists.get(j).size();++k)
                {
                    if(!found)break;
                    if(lists.get(j).get(k).page.equals(current_page))
                    {
                        if (lists.get(j).get(k).position == position+1 && (lists.get(j).get(k).origin.equals(original_query.get(j))))
                        {
                            word temp = lists.get(j).get(k);
                            int check = 1, originality = 1;
                            if (tfs.get(j) >= (temp.page_size) / 3) check = -1;
                            double word_rank = temp.rank * tfs.get(j) * idfs.get(j) * ((temp.origin.equals(original_query.get(j))) ? 2 : 1) * check;
                            if(Pages.get(current_page)==null)
                                Pages.put(current_page,new Page(current_page, (word_rank + pre_rank) / (position - temp.position), (temp.rank==0||image==1) ? 1 : 0,position));
                            else
                            {
                                Pages.get(current_page).rank+=(word_rank + pre_rank) / (position - temp.position);
                                if(Pages.get(current_page).img==0)
                                    Pages.get(current_page).img=(temp.rank==0||image==1) ? 1 : 0;
                            }
                            if(j==lists.size())
                                Pages2.put(current_page,Pages.get(current_page));
                            position = temp.position;
                            lists.get(j).remove(k);
                            if (lists.get(j).size() == 0) {
                                lists.remove(j);
                                tfs.remove(j);
                                idfs.remove(j);
                                break;
                            }
                            break;
                        }
                        else
                        {
                            lists.get(j).remove(k);
                            --k;
                        }
                    }
                    else
                    {
                        for(int z=0;z< lists.get(0).size();++z)
                        {
                            if(lists.get(0).get(z).page==current_page)
                                lists.get(0).remove(z);
                            else break;
                        }
                    }
                    if(lists.get(j).size()==0)
                        found=false;
                }
            }
            if(position==first_position&& original_query.size()==1)
                if(Pages2.get(current_page)==null) Pages2.put(current_page,new Page(current_page,pre_rank,image,position));
                else
                {
                    Pages2.get(current_page).rank+=pre_rank;
                    if(Pages2.get(current_page).img==0)
                        Pages2.get(current_page).img=1;
                }
        }
        PriorityQueue<Page> queue=new PriorityQueue<Page>();
        for (Map.Entry<String, Page> entry : Pages2.entrySet())
        {
            if((video==1&&this.urls.get(entry.getKey()).get(1)==1)||(img==1&&entry.getValue().img==1)||(img==0&&video==0))
            {
                entry.getValue().rank*=this.urls.get(entry.getKey()).get(0);
                queue.add(entry.getValue());
            }
        }
        Map<String,Integer>output=new HashMap<>();
        for(Page anObject : queue)
        {

            output.put(anObject.page,anObject.positian);
        }
        return output;
    }




}
