from gda.util import ElogEntry
from gda.data.metadata import GDAMetadataProvider

def setTitle(title):
    GDAMetadataProvider.getInstance().setMetadataValue("title", title)

def getTitle():
    return GDAMetadataProvider.getInstance().getMetadataValue("title")

def setVisit(visit):
    GDAMetadataProvider.getInstance().setMetadataValue("visit", visit)

def getVisit():
    return GDAMetadataProvider.getInstance().getMetadataValue("visit")
