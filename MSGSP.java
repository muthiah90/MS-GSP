import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Muthiah & Kamala on 9/15/2015.
 * MS-GSP Algorithm Implementation
 */
public class MSGSP {
    public static void main(String args[])
    {
        double sdcValue = 0;
        String key = null;
        double tempData = 0;
        int transactionSize = 0;
        //Map for storing Element Sets
        LinkedHashMap<Integer, ArrayList<ArrayList<ArrayList<String>>>> freqItemSet= new LinkedHashMap<>();
        LinkedHashMap<Integer, ArrayList<ArrayList<ArrayList<String>>>> freqItemSetTemp= new LinkedHashMap<>();
        ArrayList<ArrayList<ArrayList<String>>> transactionSet;
        transactionSet = ReadTransaction();
        transactionSize = transactionSet.size();
        Map<String, Double> misValues = new LinkedHashMap<>();
        //Reading MIS values along with the SDC
        misValues = ReadMISValues();
        sdcValue = misValues.get("SDC");
        misValues.remove("SDC");
        Map<String, Double> sortedMIS = SortedMIS(misValues);
        Map<String, Integer> supportCount = ItemSupports(transactionSet, sortedMIS);
        ArrayList<String> LArray = InitPass(sortedMIS, supportCount, transactionSize);
        Iterator lArrayIterator = LArray.iterator();
        ArrayList<ArrayList<ArrayList<String>>> F1 = new ArrayList<>();
        ArrayList<String> tempF1 = new ArrayList<>();
        //Generating the first level frequent set F1
        while(lArrayIterator.hasNext())
        {
            key = (String)lArrayIterator.next();
            tempData = (supportCount.get(key) * 1.0)/transactionSize;
            if(tempData >= sortedMIS.get(key))
            {
                ArrayList<String> temp1 = new ArrayList<>();
                temp1.add(key);

                ArrayList<ArrayList<String>> temp2 = new ArrayList<ArrayList<String>>();
                temp2.add(temp1);

                F1.add(temp2);
                tempF1.add(key);
            }
        }
        freqItemSet.put(1, F1);

        //Generating frequent sets of level 2 and above
        for(int i=2; freqItemSet.get(i-1).size() != 0; i++)
        {
            if(i == 2)
            {
                //This is to call the level 2 candidate generation function
                freqItemSet.put(i, CandidateGenerationLevel2(LArray, supportCount, sortedMIS, transactionSize, sdcValue, tempF1 ));
            }
            else
            {
                //Call the candidate generation function for level 3 and above
                freqItemSet.put(i, CandidateGeneration(freqItemSet.get(i - 1), sortedMIS));
                freqItemSet.put(i, prune(freqItemSet.get(i - 1), freqItemSet.get(i), sortedMIS));

            }

            //Extracting the frequent item sets from the candidate sets based on their occurrence in the transaction set
            ArrayList<ArrayList<ArrayList<String>>> prunedCandidates = new ArrayList<ArrayList<ArrayList<String>>>(freqItemSet.get(i));
            ArrayList<ArrayList<ArrayList<String>>> prunedCandidatesNew = new ArrayList<ArrayList<ArrayList<String>>>();
            int candItemCount = 0;
            double supportValue = 0;
            for(int k=0; k<prunedCandidates.size(); k++)
            {
                candItemCount = 0;
                for(int l=0; l<transactionSize; l++)
                {
                    if(SubsetMatcher(transactionSet.get(l), prunedCandidates.get(k)))
                    {
                        candItemCount++;
                    }
                }

                supportValue = (candItemCount * 1.0)/transactionSize;
                if(supportValue >= sortedMIS.get(MinMISValue(prunedCandidates.get(k), sortedMIS)))
                {
                    prunedCandidatesNew.add(prunedCandidates.get(k));
                }
            }

            freqItemSet.put(i, prunedCandidatesNew);
            if(prunedCandidatesNew.size() != 0)
            {
                freqItemSet.put(i, RemoveDuplicates(prunedCandidatesNew));
            }
            freqItemSetTemp.put(i, prunedCandidatesNew);
        }

        //This portion is to print the frequent patterns in the output file
        try
        {
            boolean firstFlag = true;
            FileWriter fileWriter = new FileWriter("E:\\Data and Text Mining\\Demo Day\\Output\\result1-2.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.flush();
            bufferedWriter.write("-------------------------- Output --------------------------------");
            bufferedWriter.newLine();
            int patternCount = 0;
            bufferedWriter.write("The number of length 1 sequential pattern is " + freqItemSet.get(1).size());
            bufferedWriter.newLine();
            for(int s = 0; s < tempF1.size(); s++)
            {
                bufferedWriter.write("Pattern: <{" + tempF1.get(s) + "}>  Count: " + supportCount.get(tempF1.get(s)));
                bufferedWriter.newLine();
            }
            for(int i=2; i<freqItemSet.size(); i++)
            {
                bufferedWriter.newLine();
                bufferedWriter.write("The number of length " + i + " sequential pattern is " + freqItemSet.get(i).size());
                bufferedWriter.newLine();
                ArrayList<ArrayList<ArrayList<String>>> finalPattern = new ArrayList<ArrayList<ArrayList<String>>>(freqItemSet.get(i));
                for(int k=0; k<finalPattern.size(); k++)
                {
                    patternCount = 0;
                    for (int l = 0; l < transactionSize; l++)
                    {
                        if (SubsetMatcher(transactionSet.get(l), finalPattern.get(k)))
                        {
                            patternCount++;
                        }
                    }

                    bufferedWriter.write("Pattern: <");
                    Iterator patItr = finalPattern.get(k).iterator();
                    while(patItr.hasNext())
                    {
                        bufferedWriter.write("{");
                        ArrayList<String> outTempSeq = new ArrayList<String>((ArrayList<String>)patItr.next());
                        Iterator sqItr = outTempSeq.iterator();
                        firstFlag = true;

                        while(sqItr.hasNext())
                        {
                            if(!firstFlag)
                            {
                                bufferedWriter.write(",");
                            }
                            firstFlag = false;
                            bufferedWriter.write((String)sqItr.next());
                        }
                        bufferedWriter.write("}");
                    }
                    bufferedWriter.write("> Count: " + patternCount);
                    bufferedWriter.newLine();
                }

            }
            bufferedWriter.close();
            fileWriter.close();
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
        int patternCount = 0;
        System.out.println("Level 1, Size: " + freqItemSet.get(1).size());
        for(int s = 0; s < tempF1.size(); s++)
        {
            System.out.println("Pattern: " + tempF1.get(s) + "  Count: " + supportCount.get(tempF1.get(s)));
        }
        for(int i=2; i<freqItemSet.size(); i++)
        {
            System.out.println("Level " + i + ", Size: " + freqItemSet.get(i).size());
            ArrayList<ArrayList<ArrayList<String>>> finalPattern = new ArrayList<ArrayList<ArrayList<String>>>(freqItemSet.get(i));
            for(int k=0; k<finalPattern.size(); k++)
            {
                patternCount = 0;
                for (int l = 0; l < transactionSize; l++)
                {
                    if (SubsetMatcher(transactionSet.get(l), finalPattern.get(k)))
                    {
                        patternCount++;
                    }
                }
                System.out.println("Pattern: " + finalPattern.get(k) + "  Count: " + patternCount);
            }

        }
    }

    /**
     * This method is to get the element with the least MIS amongst the given sequence set
     * @param itemSet
     * @param misValue
     * @return
     */
    public static String MinMISValue (ArrayList<ArrayList<String>> itemSet, Map<String, Double> misValue)
    {
        ArrayList<String> tempList = new ArrayList<String>(getAllElements(itemSet));
        Iterator itr = tempList.iterator();
        double minValue = 0, tempValue = 0;
        String key = null, tempKey = null;
        if(itr.hasNext())
        {
            key = (String)itr.next();
            minValue = misValue.get(key);

            while(itr.hasNext())
            {
                tempKey = (String)itr.next();
                tempValue = misValue.get(tempKey);

                if(tempValue < minValue)
                {
                    key = tempKey;
                    minValue = tempValue;
                }
            }
        }

        return key;
    }

    /**
     * This element checks if the sub elements of set2 is contained in the sub elements of set1
     * @param setOne
     * @param setTwo
     * @return
     */
    public static boolean SubsetMatcher (ArrayList<ArrayList<String>> setOne, ArrayList<ArrayList<String>> setTwo)
    {
        boolean flag = false;
        int j=0, matchCount = 0;

        if(setTwo.size() <= setOne.size())
        {
            for(int i=0; (i<setOne.size()&& j<setTwo.size());i++)
            {
                if(setOne.get(i).containsAll(setTwo.get(j)))
                {
                    matchCount++;
                    j++;
                }
            }

            if(matchCount == setTwo.size())
            {
                flag  = true;
            }
        }
        return flag;
    }

    /**
     * To read all the transaction from the file and store it in an array format
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> ReadTransaction()
    {
        ArrayList<ArrayList<ArrayList<String>>> transactionSet = new ArrayList<ArrayList<ArrayList<String>>>();
        try
        {
            FileReader fileReader = new FileReader("E:\\Data and Text Mining\\Demo Day\\test-data\\small-data-1\\data-1.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String newLine = null;
            String[] tempSeq = null;


            Pattern pattern = Pattern.compile("\\{(.*?)\\}");
            Matcher match = null;

            while((newLine = bufferedReader.readLine()) != null)
            {
                ArrayList<ArrayList<String>> transaction = new ArrayList<ArrayList<String>>();
                match = pattern.matcher(newLine);

                while(match.find())
                {
                    ArrayList<String> sequence = new ArrayList<String>();
                    tempSeq = match.group(1).split(",");
                    for(int i=0; i<tempSeq.length; i++)
                    {
                        sequence.add((tempSeq[i]).trim());
                    }
                    transaction.add(sequence);
                    tempSeq = null;
                }

                transactionSet.add(transaction);
            }
        }
        catch(IOException e)
        {
            System.out.println(e);
        }

        return transactionSet;
    }

    /**
     * This method is to read the MIS values for all the elements from the corresponding file
     * @return
     */
    public static Map<String, Double> ReadMISValues()
    {
        Map<String, Double> misValues = new LinkedHashMap<String, Double>();
        try
        {
            FileReader fileReader = new FileReader("E:\\Data and Text Mining\\Demo Day\\test-data\\small-data-1\\para1-2.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String tempLine = null;

            Pattern pattern = Pattern.compile("\\((.*?)\\)");
            Pattern valuePattern = Pattern.compile("=(.*)");
            Pattern diffPattern = Pattern.compile("SDC");
            Matcher matcher, valueMatcher, diffMatcher = null;

            while((tempLine = bufferedReader.readLine()) != null)
            {
                matcher = pattern.matcher(tempLine);
                valueMatcher = valuePattern.matcher(tempLine);
                diffMatcher = diffPattern.matcher(tempLine);

                if(matcher.find())
                {
                    if(valueMatcher.find())
                    {
                        misValues.put((matcher.group(1)).trim(), Double.parseDouble((valueMatcher.group(1)).trim()));
                    }
                }
                else
                {
                    if(diffMatcher.find())
                    {
                        if(valueMatcher.find())
                        {
                            misValues.put("SDC", Double.parseDouble((valueMatcher.group(1)).trim()));
                        }
                    }
                }
            }
        }
        catch(IOException e)
        {
            System.out.println(e);
        }

        return misValues;
    }

    /**
     * Method to sort all the elements based on their MIS values in ascending order
     * @param inputMap
     * @return
     */
    public static Map<String, Double> SortedMIS(Map<String, Double> inputMap)
    {
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();

        Iterator itr;
        String key, tempKey = null;
        Double keyValue, tempKeyValue = null;
        inputMap.remove("SDC");
        while(inputMap.size() >= 1)
        {
            itr = inputMap.keySet().iterator();
            key = (String)itr.next();
            keyValue = inputMap.get(key);
            while(itr.hasNext())
            {
                tempKey = (String)itr.next();
                tempKeyValue = inputMap.get(tempKey);
                if(tempKeyValue < keyValue)
                {
                    key = tempKey;
                    keyValue = tempKeyValue;
                }
            }
            sortedMap.put(key, keyValue);
            inputMap.remove(key);
        }
        return sortedMap;
    }

    /**
     * Method to get the individual item support for each item
     * @param transactionSet
     * @param misValues
     * @return
     */
    public static Map<String, Integer> ItemSupports(ArrayList<ArrayList<ArrayList<String>>> transactionSet, Map<String, Double> misValues)
    {
        Map<String, Integer> supportCount = new LinkedHashMap<>();
        ArrayList<ArrayList<String>> secondLevelTemp;
        ArrayList<String> sequence;
        String key;
        int tempCount;
        Iterator misIterator = misValues.keySet().iterator();
        Iterator transactionSetIterator, levelTwoIterator, levelOneIterator;
        Boolean itemFound;

        while(misIterator.hasNext())
        {
            key = ((String)misIterator.next()).trim();
            transactionSetIterator = transactionSet.iterator();
            tempCount = 0;
            while(transactionSetIterator.hasNext())
            {
                secondLevelTemp = (ArrayList<ArrayList<String>>)transactionSetIterator.next();
                levelTwoIterator = secondLevelTemp.iterator();
                itemFound = false;
                while(levelTwoIterator.hasNext() && !(itemFound))
                {
                    sequence = (ArrayList<String>)levelTwoIterator.next();
                    levelOneIterator = sequence.iterator();
                    while(levelOneIterator.hasNext())
                    {
                        if(key.equalsIgnoreCase(((String)levelOneIterator.next()).trim()))
                        {
                            tempCount++;
                            itemFound = true;
                            break;
                        }
                    }

                }
            }
            supportCount.put(key, tempCount);
        }
        return supportCount;
    }

    /**
     * This method is to make an initial pass through the transaction and form the L Array
     * @param sortedMIS
     * @param supportCount
     * @param transactionCount
     * @return
     */
    public static ArrayList<String> InitPass(Map<String, Double> sortedMIS, Map<String, Integer> supportCount, int transactionCount)
    {
        ArrayList<String> LArray = new ArrayList<>();
        String key;
        Boolean minFlag = false;
        double minValue = 0, tempData = 0;
        Iterator misIterator = sortedMIS.keySet().iterator();
        while(misIterator.hasNext())
        {
            key = (String)misIterator.next();
            tempData = (supportCount.get(key)*1.0)/transactionCount;
            if(!minFlag)
            {
                if(tempData >= sortedMIS.get(key))
                {
                    minFlag = true;
                    LArray.add(key);
                    minValue = sortedMIS.get(key);
                }
            }
            else
            {
                if(tempData >= minValue)
                {
                    LArray.add(key);
                }
            }
        }
        return LArray;
    }

    /**
     * This method generates the Level 2 candidates and also prunes them to form the level 2 candidates
     * @param LArray
     * @param supportMap
     * @param misValues
     * @param transactionSize
     * @param sdc
     * @param F1
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> CandidateGenerationLevel2(ArrayList<String> LArray, Map<String, Integer> supportMap, Map<String, Double> misValues, int transactionSize, double sdc, ArrayList<String> F1)
    {
        Iterator firstLevelItr = LArray.iterator();
        Iterator secondLevelItr;
        int firstLevelCounter = 0, secondLevelCounter = 0;
        String firstKey = null, secondKey = null;
        double firstSupport = 0, secondSupport = 0, supportDifference = 0;
        ArrayList<ArrayList<ArrayList<String>>> secondLevelCandidate = new ArrayList<ArrayList<ArrayList<String>>>();

        while(firstLevelItr.hasNext())
        {
            firstKey = (String)firstLevelItr.next();
            firstLevelCounter++;
            firstSupport = (supportMap.get(firstKey) * 1.0) / transactionSize;
            if(firstSupport >= misValues.get(firstKey))
            {
                secondLevelItr = LArray.iterator();
                secondLevelCounter = 0;
                while(secondLevelItr.hasNext())
                {
                    secondKey = (String)secondLevelItr.next();
                    secondLevelCounter++;

                    if(secondLevelCounter > firstLevelCounter)
                    {
                        secondSupport = (supportMap.get(secondKey) * 1.0) / transactionSize;

                        if((secondSupport >= misValues.get(firstKey)) && (Math.abs(secondSupport - firstSupport) <= sdc))
                        {
                            // This is to produce the combination {{x} {y}}
                            ArrayList<String> firstItem = new ArrayList<>();
                            firstItem.add(firstKey);

                            ArrayList<String> secondItem = new ArrayList<>();
                            secondItem.add(secondKey);

                            ArrayList<ArrayList<String>> firstSequence = new ArrayList<ArrayList<String>>();
                            firstSequence.add(firstItem);
                            firstSequence.add(secondItem);

                            //This is to produce the combination {{y} {x}}
                            ArrayList<ArrayList<String>> reverseSequence = new ArrayList<ArrayList<String>>();
                            reverseSequence.add(secondItem);
                            reverseSequence.add(firstItem);

                            //This is to produce the combination {{x,y}}
                            ArrayList<String> combinedItem = new ArrayList<String>();
                            combinedItem.add(firstKey);
                            combinedItem.add(secondKey);

                            ArrayList<ArrayList<String>> secondSequence = new ArrayList<ArrayList<String>>();
                            secondSequence.add(combinedItem);

                            //Adding all the generated combination to the candidate key set
                            secondLevelCandidate.add(firstSequence);
                            secondLevelCandidate.add(secondSequence);
                            secondLevelCandidate.add(reverseSequence);


                        }

                    }
                }
            }

        }

        //System.out.println(secondLevelCandidate);
        //Pruning the generated Level 2 candidate seqeuences
        //The sequence is removed only if the element with the lowest MIS value is not found in the Level 1 frequent items
        String tempData = null;
        boolean flag = false;
        Iterator thirdLevel = secondLevelCandidate.iterator();
        ArrayList<ArrayList<ArrayList<String>>> prunedLevelTwoCandidates = new ArrayList<ArrayList<ArrayList<String>>>();
        while(thirdLevel.hasNext())
        {
            ArrayList<ArrayList<String>> sequenceSets = (ArrayList<ArrayList<String>>)thirdLevel.next();
            flag = true;
            ArrayList<String> checkString = new ArrayList<String>(getAllElements(sequenceSets));
            Iterator iterator = checkString.iterator();
            boolean first = true;
            double minEle = 0;
            int smaller = 0;
            while(iterator.hasNext())
            {
                String element = (String)iterator.next();
                if(first)
                {
                    minEle = misValues.get(element);
                    smaller = 0;
                    first = false;
                }
                else
                {
                    if(misValues.get(element) < minEle)
                    {
                        smaller = 1;
                    }
                }
            }
            if(F1.indexOf(checkString.get(smaller)) < 0)
                {
                flag = false;
            }
            if(flag)
            {
                prunedLevelTwoCandidates.add(sequenceSets);
            }

        }
        return prunedLevelTwoCandidates;
    }

    /**
     * Method to generate candidate keys from Level 3 and above
     * @param frequentSet
     * @param misValues
     */
    public static ArrayList<ArrayList<ArrayList<String>>> CandidateGeneration(ArrayList<ArrayList<ArrayList<String>>> frequentSet, Map<String, Double> misValues)
    {
        ArrayList<ArrayList<ArrayList<String>>> candidates = new ArrayList<ArrayList<ArrayList<String>>>();
        for(int i = 0; i < frequentSet.size(); i++) //frequentSet.size()
        {
            for(int j = 0; j < frequentSet.size(); j++)//j = i+1
            {
                ArrayList<ArrayList<String>> itemSet1 = new ArrayList<ArrayList<String>>(frequentSet.get(i));
                ArrayList<ArrayList<String>> itemSet2 = new ArrayList<ArrayList<String>>(frequentSet.get(j));
                ArrayList<ArrayList<ArrayList<String>>> interCandSet = new ArrayList<ArrayList<ArrayList<String>>>();

                //This loops through the 3 specific conditions mentioned in the algorithm and calls the respective joining step
                if(isFirstElementLowestMIS(itemSet1, misValues))
                {
                    if(FirstCondition(itemSet1,itemSet2) && SecondCondition(itemSet1, itemSet2, misValues))
                    {
                        interCandSet = FirstLeastJoin(itemSet1, itemSet2, misValues);
                    }
                }
                else if(isLastElementLowestMIS(itemSet2, misValues))
                {
                    if(FirstConditionReverse(itemSet1, itemSet2) && SecondConditionReverse(itemSet1, itemSet2, misValues))
                    {
                        interCandSet = LastLeastJoin(itemSet1, itemSet2, misValues);
                    }
                }
                else
                {
                    if(JoinCondition(itemSet1, itemSet2))
                    {
                        interCandSet = GeneralJoin(itemSet1, itemSet2);
                    }
                }

                Iterator candItr = interCandSet.iterator();
                while(candItr.hasNext())
                {
                    candidates.add((ArrayList<ArrayList<String>>)candItr.next());
                }
            }
        }
        return candidates;
    }

    /**
     * This method is to delete all the duplicate item sets in the generated sequence
     * @param prunedPatterns
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> RemoveDuplicates (ArrayList<ArrayList<ArrayList<String>>> prunedPatterns)
    {
        ArrayList<ArrayList<ArrayList<String>>> freshPatterns = new ArrayList<ArrayList<ArrayList<String>>>();
        freshPatterns.add(prunedPatterns.get(0));
        boolean flag = true;
        int count = 0;

        for (int i = 1; i < prunedPatterns.size(); i++)
        {
            ArrayList<ArrayList<String>> tempSequence = new ArrayList<ArrayList<String>>(prunedPatterns.get(i));
            flag = true;

            for(int j=0; j<freshPatterns.size(); j++)
            {
                ArrayList<ArrayList<String>> compSeq = new ArrayList<ArrayList<String>>(freshPatterns.get(j));
                count = 0;
                if(tempSequence.size() == compSeq.size())
                {
                    for(int k=0; k < tempSequence.size(); k++)
                    {
                        if(tempSequence.get(k).equals(compSeq.get(k)))
                        {
                            count++;
                        }
                    }

                    if(count == tempSequence.size())
                    {
                        flag = false;
                        break;
                    }
                }
            }

            if(flag)
            {
                freshPatterns.add(prunedPatterns.get(i));
            }
        }
        return freshPatterns;
    }

    /**
     * Method to check if the first element in a sequence is the lowest among all the elements
     * @param itemSet
     * @param misValues
     * @return
     */
    public static boolean isFirstElementLowestMIS(ArrayList<ArrayList<String>> itemSet, Map<String, Double> misValues)
    {
        boolean flag = true, firstElement = true;
        double minValue = 0;
        String key = null;

        for(int i = 0; i < itemSet.size(); i++)
        {
            ArrayList<String> sequence = (ArrayList<String>)itemSet.get(i);

            Iterator sequenceIterator = sequence.iterator();
            while(sequenceIterator.hasNext())
            {
                key = (String)sequenceIterator.next();
                if(firstElement)
                {
                    minValue = misValues.get(key);
                    firstElement = false;
                }
                else
                {
                    if(misValues.get(key) < minValue)
                    {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * Method to check if the last element in a sequence is the lowest of the lot
     * @param itemSet
     * @param misValues
     * @return
     */
    public static boolean isLastElementLowestMIS(ArrayList<ArrayList<String>> itemSet, Map<String, Double> misValues)
    {
        boolean flag = true, firstElement = true;
        double minValue = 0;
        String key = null;

        for (int i=itemSet.size() - 1; i >= 0; i--)
        {
            ArrayList<String> sequence = (ArrayList<String>)itemSet.get(i);

            for(int j = sequence.size() - 1; j >= 0 ; j--)
            {
                key = sequence.get(j);
                if(firstElement)
                {
                    minValue = misValues.get(key);
                    firstElement = false;
                }
                else
                {
                    if(misValues.get(key) < minValue)
                    {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * Method to check if the subsequences obtained by dropping the second item in S1 and the last item of S2 are the same
     * @param calcItemSet1
     * @param calcItemSet2
     * @return
     */
    public static boolean FirstCondition (ArrayList<ArrayList<String>> calcItemSet1, ArrayList<ArrayList<String>> calcItemSet2)
    {
        ArrayList<String> firstSequence = new ArrayList<String>(getAllElements(calcItemSet1));
        ArrayList<String> secondSequence = new ArrayList<String>(getAllElements(calcItemSet2));
        boolean returnFlag = false;

        if((firstSequence.size() > 1) && (secondSequence.size() > 0))
        {
            firstSequence.remove(1);
            secondSequence.remove(secondSequence.size() -1);

            if(firstSequence.equals(secondSequence))
            {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /**
     * This method is to check if the subsequences obtained by dropping the second last item in S2 and the first item in S1 are the same
     * @param calcItemSet1
     * @param calcItemSet2
     * @return
     */
    public static boolean FirstConditionReverse (ArrayList<ArrayList<String>> calcItemSet1, ArrayList<ArrayList<String>> calcItemSet2)
    {
        ArrayList<String> firstSeq = new ArrayList<String>(getAllElements(calcItemSet1));
        ArrayList<String> secondSeq = new ArrayList<String>(getAllElements(calcItemSet2));
        boolean returnFlag = false;

        if((secondSeq.size() > 1) && (firstSeq.size() > 0))
        {
            secondSeq.remove(secondSeq.size() - 2);
            firstSeq.remove(0);

            if(firstSeq.equals(secondSeq))
            {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /**
     * Method to check if the MIS value of the last item of S2 is greater than that of the first item of S1
     * @param c1
     * @param c2
     * @param misValues
     * @return
     */
    public static boolean SecondCondition (ArrayList<ArrayList<String>> c1, ArrayList<ArrayList<String>> c2, Map<String, Double> misValues)
    {
        ArrayList<String> firstSequence = new ArrayList<String>(getAllElements(c1));
        ArrayList<String> secondSequence = new ArrayList<String>(getAllElements(c2));
        boolean returnFlag = false;

        if((firstSequence.size() > 0) && (secondSequence.size() > 0))
        {
            double firstMisValue = misValues.get(firstSequence.get(0));
            double secondMisValue = misValues.get(secondSequence.get(secondSequence.size() - 1));

            if(secondMisValue > firstMisValue)
            {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /**
     * Method to check if the MIS value of the last item of S2 is lesser than that of the first item of S1
     * @param c1
     * @param c2
     * @param misValues
     * @return
     */
    public static boolean SecondConditionReverse (ArrayList<ArrayList<String>> c1, ArrayList<ArrayList<String>> c2, Map<String, Double> misValues)
    {
        ArrayList<String> firstSequence = new ArrayList<String>(getAllElements(c1));
        ArrayList<String> secondSequence = new ArrayList<String>(getAllElements(c2));
        boolean returnFlag = false;

        if((firstSequence.size() > 0) && (secondSequence.size() > 0))
        {
            double secondMisValue = misValues.get(secondSequence.get(secondSequence.size() - 1));
            double firstMisValue = misValues.get(firstSequence.get(0));

            if(firstMisValue > secondMisValue)
            {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /**
     * Method to check if the sequence obtained by dropping the first element in the first sequence and dropping the last element in the second sequence, is the same.
     * @param fSequence
     * @param tSequence
     * @return
     */
    public static boolean JoinCondition(ArrayList<ArrayList<String>> fSequence, ArrayList<ArrayList<String>> tSequence)
    {
        ArrayList<String> allFirstSequence = new ArrayList<String>(getAllElements(fSequence));
        ArrayList<String> allSecondSequence = new ArrayList<String>(getAllElements(tSequence));
        boolean returnFlag = false;

        if(allFirstSequence.size() > 0 && allSecondSequence.size() > 0)
        {
            allFirstSequence.remove(0);
            allSecondSequence.remove(allSecondSequence.size() - 1);

            if(allFirstSequence.equals(allSecondSequence))
            {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /**
     * Method to get all elements from a two dimensional array into a single array
     * @param inputSetList
     * @return
     */
    public static ArrayList<String> getAllElements(ArrayList<ArrayList<String>> inputSetList)
    {
        ArrayList<String> tempArray = new ArrayList<String>();

        Iterator tempIte = inputSetList.iterator();
        while(tempIte.hasNext())
        {
            ArrayList<String> seq = new ArrayList<String>((ArrayList<String>)tempIte.next());
            Iterator innerItr = seq.iterator();
            while(innerItr.hasNext())
            {
                tempArray.add((String)innerItr.next());
            }
        }

        return tempArray;
    }

    /**
     * This method produces candidate keys for the sequences where the first element of the first sequence has the lowest MIS amongst all the elements in the same set
     * @param set1
     * @param set2
     * @param misValues
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> FirstLeastJoin(ArrayList<ArrayList<String>> set1, ArrayList<ArrayList<String>> set2, Map<String, Double> misValues)
    {
        ArrayList<ArrayList<ArrayList<String>>> returnCandidates = new ArrayList<ArrayList<ArrayList<String>>>();
        if(set2.get(set2.size() - 1).size() == 1)
        {
            ArrayList<ArrayList<String>> newCandidate = new ArrayList<ArrayList<String>>(set1);
            ArrayList<String> tempSequence = new ArrayList<String>(set2.get(set2.size() - 1));
            newCandidate.add(tempSequence);
            returnCandidates.add(newCandidate);

            if((PatternLength(set1) == 2) && (set1.size() == 2) && LastTwoGreater(set1, set2, misValues))
            {
                String lastElemet = set2.get(set2.size() - 1).get(set2.get(set2.size() - 1).size() - 1);
                ArrayList<ArrayList<String>> extraCandidate = new ArrayList<ArrayList<String>>(set1);
                ArrayList<String> tempExtra = new ArrayList<String>(set1.get(set1.size() - 1));
                tempExtra.add(lastElemet);
                extraCandidate.remove(extraCandidate.size() - 1);
                extraCandidate.add(tempExtra);
                returnCandidates.add(extraCandidate);
            }
        }
        else if(((PatternLength(set1) == 2) && (set1.size() == 1) && LastTwoGreater(set1, set2, misValues)) || (PatternLength(set1) > 2))
        {
            ArrayList<String> tempSet2 = new ArrayList<String>(set2.get(set2.size() - 1));
            ArrayList<ArrayList<String>> newCandidate2 = new ArrayList<ArrayList<String>>(set1);
            ArrayList<String> tempSeq1 = new ArrayList<String>(newCandidate2.get(newCandidate2.size() - 1));
            tempSeq1.add(tempSet2.get(tempSet2.size() - 1));
            newCandidate2.remove(newCandidate2.size() - 1);
            newCandidate2.add(tempSeq1);
            returnCandidates.add(newCandidate2);
        }
        return returnCandidates;
    }

    /**
     * This method produces candidate keys for the sequences where the last element of the second sequence has the lowest MIS amongst all the elements in the same set
     * @param set1
     * @param set2
     * @param misValues
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> LastLeastJoin(ArrayList<ArrayList<String>> set1, ArrayList<ArrayList<String>> set2, Map<String, Double> misValues)
    {
        ArrayList<ArrayList<ArrayList<String>>> leastJoinCandidates = new ArrayList<ArrayList<ArrayList<String>>>();
        if(set1.get(0).size() == 1)
        {
            ArrayList<ArrayList<String>> LLCandidate = new ArrayList<ArrayList<String>>();
            LLCandidate.add(set1.get(0));
            Iterator set2Itr = set2.iterator();
            while(set2Itr.hasNext())
            {
                LLCandidate.add((ArrayList<String>)set2Itr.next());
            }
            leastJoinCandidates.add(LLCandidate);

            if((PatternLength(set2) == 2) && (set2.size() == 2) && FirstTwoGreater(set1, set2, misValues))
            {
                ArrayList<ArrayList<String>> sequenceList = new ArrayList<ArrayList<String>>();
                ArrayList<String> tempList = new ArrayList<String>(set2.get(0));
                ArrayList<String> newSequence = new ArrayList<String>();
                newSequence.add(set1.get(0).get(0));
                Iterator setItr = tempList.iterator();
                while(setItr.hasNext())
                {
                    newSequence.add((String)setItr.next());
                }

                sequenceList.add(newSequence);
                setItr = set2.iterator();
                setItr.next();
                while (setItr.hasNext())
                {
                    sequenceList.add((ArrayList<String>)setItr.next());
                }
                leastJoinCandidates.add(sequenceList);
            }
        }
        else if(((PatternLength(set2) == 2) && set2.size() == 1 && FirstTwoGreater(set1, set2, misValues)) || (PatternLength(set2) > 2))
        {
            ArrayList<String> setOneSeq = new ArrayList<String>(set1.get(0));
            ArrayList<String> setTwoSeq = new ArrayList<String>(set2.get(0));
            ArrayList<String> genSequence = new ArrayList<String>();
            ArrayList<ArrayList<String>> genSequenceSet = new ArrayList<ArrayList<String>>();
            genSequence.add(setOneSeq.get(0));
            Iterator setTwoItr = setTwoSeq.iterator();
            while(setTwoItr.hasNext())
            {
                genSequence.add((String)setTwoItr.next());
            }

            genSequenceSet.add(genSequence);
            setTwoItr = set2.iterator();
            setTwoItr.next();
            while(setTwoItr.hasNext())
            {
                genSequenceSet.add((ArrayList<String>)setTwoItr.next());
            }
            leastJoinCandidates.add(genSequenceSet);
        }
        return leastJoinCandidates;
    }

    /**
     * This method does the general join based on the size of the last sequence in the second set thats passed to the function
     * @param set1
     * @param set2
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> GeneralJoin (ArrayList<ArrayList<String>> set1, ArrayList<ArrayList<String>> set2)
    {
        ArrayList<ArrayList<ArrayList<String>>> generalCandidate = new ArrayList<ArrayList<ArrayList<String>>>();

        if(set2.get(set2.size() - 1).size() == 1)
        {
            ArrayList<ArrayList<String>> tmpCandidate = new ArrayList<ArrayList<String>>(set1);
            ArrayList<String> tmpSequence = new ArrayList<String>(set2.get(set2.size() - 1));
            tmpCandidate.add(tmpSequence);
            generalCandidate.add(tmpCandidate);
        }
        else
        {
            ArrayList<ArrayList<String>> joinCandiate = new ArrayList<ArrayList<String>>(set1);
            ArrayList<String> newSeq = new ArrayList<String>(set1.get(set1.size() - 1));
            newSeq.add(set2.get(set2.size() - 1).get(set2.get(set2.size() - 1).size() - 1));
            joinCandiate.remove(joinCandiate.size() - 1);
            joinCandiate.add(newSeq);
            generalCandidate.add(joinCandiate);
        }
        return generalCandidate;
    }

    /**
     * Returns the length of a given pattern
     * @param targetPattern
     * @return
     */
    public static int PatternLength(ArrayList<ArrayList<String>> targetPattern)
    {
        int length = 0;
        Iterator itr = targetPattern.iterator();
        while (itr.hasNext())
        {
            ArrayList<String> interList = (ArrayList<String>)itr.next();
            length = length + interList.size();
        }
        return length;
    }

    /**
     * This function checks if the MIS value of last item of S2 is greater than the MIS value of the last item of S1
     * @param patternOne
     * @param patternTwo
     * @return
     */
    public static boolean LastTwoGreater(ArrayList<ArrayList<String>> patternOne, ArrayList<ArrayList<String>> patternTwo, Map<String, Double> misValues)
    {
        ArrayList<String> firstSequence = new ArrayList<String>(getAllElements(patternOne));
        ArrayList<String> secondSequence = new ArrayList<String>(getAllElements(patternTwo));
        boolean returnFlag = false;

        if((firstSequence.size() > 0) && (secondSequence.size() > 0))
        {
            double firstMisValue = misValues.get(firstSequence.get(firstSequence.size() - 1));
            double secondMisValue = misValues.get(secondSequence.get(secondSequence.size() - 1));

            if(secondMisValue > firstMisValue)
            {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /**
     * This reverses the "LastTwoGreater" function
     * This function checks if the MIS value of the first item of S1 is greater than the MIS value of the first item of S2
     * @param patternOne
     * @param patternTwo
     * @param misValues
     * @return
     */
    public static boolean FirstTwoGreater(ArrayList<ArrayList<String>> patternOne, ArrayList<ArrayList<String>> patternTwo, Map<String, Double> misValues)
    {
        ArrayList<String> firstSequence = new ArrayList<String>(getAllElements(patternOne));
        ArrayList<String> secondSequence = new ArrayList<String>(getAllElements(patternTwo));
        boolean returnFlag = false;

        if((firstSequence.size() > 0) && (secondSequence.size() > 0))
        {
            double firstMisValue = misValues.get(firstSequence.get(0));
            double secondMisValue = misValues.get(secondSequence.get(0));

            if(firstMisValue > secondMisValue)
            {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /**
     * Main Pruning method which calls all the sub methods involved in the pruning step
     * @param FKminus1
     * @param FK
     * @param Mis
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> prune(ArrayList<ArrayList<ArrayList<String>>> FKminus1, ArrayList<ArrayList<ArrayList<String>>> FK , Map<String,Double> Mis )
    {
        ArrayList<ArrayList<String>> sequence;
        ArrayList<ArrayList<ArrayList<String>>> subseq;
        for(int i=0; i<FK.size();i++)
        {
            sequence = FK.get(i);
            subseq = generateSubseq(sequence);
            subseq = removeUnwantedSubseq(sequence, subseq,Mis);
            if(check(subseq,FKminus1)==false)
            {
                FK.remove(i);
            }
        }
        return FK;
    }

    /**
     * This method is to check if the generated sub sequences is present in the previous frequent item sets (F k-1)
     * @param subseq
     * @param FKminus1
     * @return
     */
    public static boolean check(ArrayList<ArrayList<ArrayList<String>>> subseq,ArrayList<ArrayList<ArrayList<String>>> FKminus1)
    {
        return(FKminus1.containsAll(subseq));
    }

    /**
     * This method is to remove the subsequences that doesnt have the minimum elememt of the lot in it.
     * @param superseq
     * @param mulseq
     * @param Mis
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> removeUnwantedSubseq(ArrayList<ArrayList<String>> superseq,ArrayList<ArrayList<ArrayList<String>>> mulseq,Map<String,Double> Mis )
    {
        ArrayList<ArrayList<ArrayList<String>>> actualSubsequences = new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<ArrayList<String>> tempSubSeq = new ArrayList<ArrayList<String>>();
        ArrayList<String> singleSubSeq = new ArrayList<String>();
        ArrayList<String> singleSet = new ArrayList<String>(getAllElements(superseq));
        Iterator singleSetItr = singleSet.iterator();
        double minValue = 0;
        String key = null, tempKey = null;

        if(singleSetItr.hasNext())
        {
            key = (String)singleSetItr.next();
            minValue = Mis.get(key);

            while(singleSetItr.hasNext())
            {
                tempKey = (String)singleSetItr.next();
                if(Mis.get(tempKey) < minValue)
                {
                    key = tempKey;
                    minValue = Mis.get(key);
                }
            }
        }

        Iterator subSeqItr = mulseq.iterator();
        while(subSeqItr.hasNext())
        {
            tempSubSeq = (ArrayList<ArrayList<String>>)subSeqItr.next();
            singleSubSeq = getAllElements(tempSubSeq);
            if(singleSubSeq.indexOf(key) >= 0)
            {
                actualSubsequences.add(tempSubSeq);
            }
        }

        return actualSubsequences;
    }

    /**
     * This method is to generate the subsequences for each of the generated candidates in all the levels
     * @param superseq
     * @return
     */
    public static ArrayList<ArrayList<ArrayList<String>>> generateSubseq(ArrayList<ArrayList<String>> superseq )
    {
        ListIterator elementit, itemit;
        ArrayList<ArrayList<ArrayList<String>>> multiplesubs = new ArrayList<ArrayList<ArrayList<String>>>() ;
        ArrayList<ArrayList<String>> sub = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> sub1,sub2;
        ArrayList<String> itemset, subitemset ;
        int i =1, tempindex, num=0;
        elementit = superseq.listIterator();

        while(elementit.hasNext())
        {
            sub1 = new ArrayList<ArrayList<String>>();
            if(elementit.hasPrevious())
            {

                tempindex = elementit.nextIndex();
                while(tempindex-- != 0)
                {
                    sub1.add(superseq.get(num));
                    num++;
                }
                num=0;
            }
            itemset = (ArrayList<String>)elementit.next();
            i=1;
            for(int j=0 ; j <itemset.size();j++)
            {
                subitemset = new ArrayList<String>(itemset);
                itemit = subitemset.listIterator();
                for(int count= i ;count>0;count--)
                {
                    if(itemit.hasNext())
                    {
                        itemit.next();
                    }
                    else
                        i=-1;
                }
                if(i==-1)break;
                itemit.remove();
                if(!(subitemset.isEmpty()))
                {
                    sub.add(subitemset);
                }
                if(elementit.hasNext())
                {
                    tempindex=elementit.nextIndex();
                    while(tempindex != superseq.size())
                    {
                        sub.add(superseq.get(tempindex));
                        tempindex++;
                    }
                }
                if(!(sub1.isEmpty()))
                {
                    sub2=new ArrayList<ArrayList<String>>();
                    sub2.addAll(sub1);
                    if(!(sub.isEmpty()))
                    {
                        sub2.addAll(sub);
                    }
                    multiplesubs.add(sub2);
                    sub2 = null;
                }
                else if (!(sub.isEmpty()))
                {
                    multiplesubs.add(sub);
                }
                sub=null;
                sub = new ArrayList<ArrayList<String>>();
                i++;
            }

        }
        return multiplesubs;
    }
}