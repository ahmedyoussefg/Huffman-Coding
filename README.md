# Huffman Compression Analysis

**For `gbbct10_old.seq` :**

| **Value Of N** | **Compression Ratio** |
| --- | --- |
| 1 | 50.665% |
| 2 | 41.984% |
| 3 | 37.632% |
| 4 | 35.571% |
| 5 | 40.003% |
- **7Zip Compression Ratio:** 24%

![image.png](./assets/image.png)

**For `Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf` :**

| **Value Of N** | **Compression Ratio** |
| --- | --- |
| 1 | 93.51% |
| 2 | 147.42% |
| 3 | 270.36% |
| 4 | 231.73% |
| 5 | 193.32% |
- **7Zip Compression Ratio:** 68%
    
    ![image.png](./assets/image%201.png)
    
    ![image.png](./assets/image%202.png)
    

> Formula used for Compression Ratio = $\frac{Size\:After\:Compression}{Size\:Before\:Compression}$
> 

## Observations And Explanations

- In case of `.seq` files, These files are editable text files that contain a DNA sequence,  so there are large patterns and repetitions so Huffman Coding works well with that case, but still 7-Zip is slightly better in that case.
- Here’s a snippet from inside the file, as you see there are large amount of repeated pattern which helps Huffman Coding algorithm.

![image.png](./assets/image%203.png)

- In case of `.pdf`  files, These files are complex files with embedded metadata, image and text.. So Huffman Coding struggles to find patterns and doesn’t work well with that case.

---

## Screenshots Running The Tests

**For `gbbct10_old.seq` :**

![image.png](./assets/image%204.png)

**At N=1:**

Compression:

![image.png](./assets/image%205.png)

Decompression:

![image.png](./assets/image%206.png)

**At N=2:**

Compression:

![image.png](./assets/image%207.png)

Decompression:

![image.png](./assets/image%208.png)

**At N=3:**

Compression:

![image.png](./assets/image%209.png)

Decompression:

![image.png](./assets/image%2010.png)

**At N=4:**

Compression:

![image.png](./assets/image%2011.png)

Decompression:

![image.png](./assets/image%2012.png)

![image.png](./assets/image%2013.png)

**At N=5:**

Compression:

![image.png](./assets/image%2014.png)

Decompression:

![image.png](./assets/image%2015.png)

---

**For `Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf` :**

![image.png](./assets/image%2016.png)

**At N=1:**

Compression:

![image.png](./assets/image%2017.png)

Decompression:

![image.png](./assets/image%2018.png)

![image.png](./assets/image%2019.png)

**At N=2:**

Compression:

![image.png](./assets/image%2020.png)

Decompression:

![image.png](./assets/image%2021.png)

**At N=3:**

Compression:

![image.png](./assets/image%2022.png)

Decompression:

![image.png](./assets/image%2023.png)

**At N=4:**

Compression:

![image.png](./assets/image%2024.png)

Decompression:

![image.png](./assets/image%2025.png)

**At N=5:**

Compression:

![image.png](./assets/image%2026.png)

Decompression:

![image.png](./assets/image%2027.png)