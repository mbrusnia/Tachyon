library(ggplot2)
mydata <-labkey.data

IMAGE_WIDTH = 1200  # in pixels
IMAGE_HEIGHT_PER_EACH_BOXPLOT = 300  # in pixels

# get indices of needed columns
NORM_DPM_IDX = which(grepl ("norm_{1,2}dpm_{1,2}mm2_{1,2}", names(mydata), perl=TRUE))
MOUSE_ID_IDX = which(grepl ("^mouseid$", names(mydata), perl=TRUE))
TISSUE_IDX = which(grepl ("^name$", names(mydata), perl=TRUE))

# if any mouse_id has less than this many measurements, we leave it off the graph, to 
# save space
MEASUREMENT_NUMBER_CUTTOFF = 5

#remove rows with NA for luminescense
mydata[, NORM_DPM_IDX] <- log10(mydata[, NORM_DPM_IDX])
mydata <- mydata[!is.na(mydata[NORM_DPM_IDX]),]  

#remove rows with NA for tissue
mydata <- mydata[!is.na(mydata[TISSUE_IDX]),] 

#remove rows with NA for MOUSE_ID
mydata <- mydata[!is.na(mydata[MOUSE_ID_IDX]),] 

#remove all mouse_ids who have less than MEASUREMENT_NUMBER_CUTTOFF measurements
mouse_ids <- unique(unlist(mydata[MOUSE_ID_IDX]))
for(i in 1:length(mouse_ids)){
	if(length(mydata[mydata[,MOUSE_ID_IDX]==mouse_ids[i],1])< MEASUREMENT_NUMBER_CUTTOFF){ 
		mydata <- mydata[!mydata[,MOUSE_ID_IDX] == mouse_ids[i],]
	}
}
mouse_ids <- unique(unlist(mydata[MOUSE_ID_IDX]))

names(mydata)[NORM_DPM_IDX] <- "Norm"
names(mydata)[TISSUE_IDX] <- "Tissue"
names(mydata)[MOUSE_ID_IDX] <- "MouseID"
png(filename="${imgout:labkeyl_png}", width=IMAGE_WIDTH, height=IMAGE_HEIGHT_PER_EACH_BOXPLOT*length(mouse_ids))
ggplot(mydata, aes(Tissue, Norm)) + geom_boxplot(fill="blue") + facet_grid(MouseID ~ .) + ggtitle("Mouse Luminescence per Tissue") + theme(axis.text.x=element_text(angle=90, vjust=0.4,hjust=1, size=14)) + labs(y="log10(Normalized DPM)")
dev.off()
